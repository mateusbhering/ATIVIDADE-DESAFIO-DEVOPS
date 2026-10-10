package br.com.fiap.cidadesesg.controller;

import br.com.fiap.cidadesesg.model.Ocorrencia;
import br.com.fiap.cidadesesg.model.TipoOcorrencia;
import br.com.fiap.cidadesesg.repository.OcorrenciaRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.Duration;
import java.time.Instant;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.matchesPattern;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Testes da API completa (controller + service + JPA) com banco H2 em memoria. */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class OcorrenciaApiIntegrationTest {

    private static final String VAZAMENTO = """
            {"tipo":"VAZAMENTO_DE_AGUA","descricao":"Agua escorrendo pela sarjeta","bairro":"Vila Nova","endereco":"Rua Sete, 88"}
            """;

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private OcorrenciaRepository repository;

    @BeforeEach
    void limparBanco() {
        repository.deleteAll();
    }

    private ResultActions abrir(String json) throws Exception {
        return mvc.perform(post("/api/ocorrencias").contentType(MediaType.APPLICATION_JSON).content(json));
    }

    private String abrirEObterProtocolo(String json) throws Exception {
        String corpo = abrir(json).andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        JsonNode node = objectMapper.readTree(corpo);
        return node.get("protocolo").asText();
    }

    private ResultActions mudarStatus(String protocolo, String json) throws Exception {
        return mvc.perform(patch("/api/ocorrencias/{p}/status", protocolo)
                .contentType(MediaType.APPLICATION_JSON).content(json));
    }

    private void salvarAbertaHa(String protocolo, TipoOcorrencia tipo, long horas) {
        repository.save(new Ocorrencia(protocolo, tipo, "teste", "Centro", "Rua A",
                Instant.now().minus(Duration.ofHours(horas))));
    }

    @Test
    @DisplayName("POST abre a ocorrência: 201, protocolo, secretaria e prazo")
    void abrirOcorrencia() throws Exception {
        abrir(VAZAMENTO)
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", matchesPattern(".*/api/ocorrencias/ZEL-[A-Z2-9]{8}")))
                .andExpect(jsonPath("$.protocolo", matchesPattern("ZEL-[A-Z2-9]{8}")))
                .andExpect(jsonPath("$.status").value("ABERTA"))
                .andExpect(jsonPath("$.secretaria").value("Companhia de Saneamento"))
                .andExpect(jsonPath("$.prazoLimite", notNullValue()))
                .andExpect(jsonPath("$.atrasada").value(false));
    }

    @Test
    @DisplayName("POST com campos obrigatórios em branco ou tipo inexistente retorna 400")
    void abrirComDadosInvalidos() throws Exception {
        abrir("""
                {"tipo":"BURACO_NA_VIA","descricao":"","bairro":"Centro","endereco":""}
                """)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem").value("descricao: descreva o problema; endereco: informe o endereço"));

        abrir("""
                {"tipo":"DISCO_VOADOR","descricao":"x","bairro":"Centro","endereco":"Rua A"}
                """)
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET por protocolo devolve a ocorrência (aceita minúsculas)")
    void buscarPorProtocolo() throws Exception {
        String protocolo = abrirEObterProtocolo(VAZAMENTO);

        mvc.perform(get("/api/ocorrencias/{p}", protocolo.toLowerCase()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.protocolo").value(protocolo))
                .andExpect(jsonPath("$.bairro").value("Vila Nova"));
    }

    @Test
    @DisplayName("GET de protocolo inexistente retorna 404")
    void protocoloInexistente() throws Exception {
        mvc.perform(get("/api/ocorrencias/ZEL-NAOEXIST"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("GET lista com filtros de status, tipo e bairro")
    void listarComFiltros() throws Exception {
        abrirEObterProtocolo(VAZAMENTO);
        abrirEObterProtocolo("""
                {"tipo":"BURACO_NA_VIA","descricao":"Buraco","bairro":"Centro","endereco":"Av. Brasil, 1"}
                """);

        mvc.perform(get("/api/ocorrencias")).andExpect(jsonPath("$", hasSize(2)));
        mvc.perform(get("/api/ocorrencias").param("bairro", "centro"))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].tipo").value("BURACO_NA_VIA"));
        mvc.perform(get("/api/ocorrencias").param("tipo", "VAZAMENTO_DE_AGUA").param("status", "ABERTA"))
                .andExpect(jsonPath("$", hasSize(1)));
        mvc.perform(get("/api/ocorrencias").param("status", "RESOLVIDA"))
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @DisplayName("PATCH percorre ABERTA → EM_ATENDIMENTO → RESOLVIDA")
    void fluxoCompletoDeAtendimento() throws Exception {
        String protocolo = abrirEObterProtocolo(VAZAMENTO);

        mudarStatus(protocolo, """
                {"status":"EM_ATENDIMENTO"}
                """)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("EM_ATENDIMENTO"))
                .andExpect(jsonPath("$.atendimentoIniciadoEm", notNullValue()));

        mudarStatus(protocolo, """
                {"status":"RESOLVIDA","observacao":"Tubulacao substituida"}
                """)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("RESOLVIDA"))
                .andExpect(jsonPath("$.encerradaEm", notNullValue()))
                .andExpect(jsonPath("$.observacao").value("Tubulacao substituida"));
    }

    @Test
    @DisplayName("PATCH que pula etapa ou reabre ocorrência retorna 409")
    void transicaoInvalidaRetorna409() throws Exception {
        String protocolo = abrirEObterProtocolo(VAZAMENTO);

        mudarStatus(protocolo, """
                {"status":"RESOLVIDA","observacao":"pulando etapa"}
                """)
                .andExpect(status().isConflict());

        mudarStatus(protocolo, """
                {"status":"CANCELADA","observacao":"Duplicada"}
                """)
                .andExpect(status().isOk());
        mudarStatus(protocolo, """
                {"status":"EM_ATENDIMENTO"}
                """)
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("PATCH para RESOLVIDA sem observação retorna 400")
    void resolverSemObservacaoRetorna400() throws Exception {
        String protocolo = abrirEObterProtocolo(VAZAMENTO);
        mudarStatus(protocolo, """
                {"status":"EM_ATENDIMENTO"}
                """);

        mudarStatus(protocolo, """
                {"status":"RESOLVIDA"}
                """)
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /atrasadas traz só pendentes vencidas, das mais atrasadas para as menos")
    void listarAtrasadas() throws Exception {
        salvarAbertaHa("ZEL-ATRASO01", TipoOcorrencia.BURACO_NA_VIA, 80);       // vencida há 8 h
        salvarAbertaHa("ZEL-ATRASO02", TipoOcorrencia.VAZAMENTO_DE_AGUA, 48);   // vencida há 24 h
        salvarAbertaHa("ZEL-NOPRAZO1", TipoOcorrencia.CALCADA_DANIFICADA, 80);  // prazo de 120 h

        mvc.perform(get("/api/ocorrencias/atrasadas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].protocolo").value("ZEL-ATRASO02"))
                .andExpect(jsonPath("$[1].protocolo").value("ZEL-ATRASO01"))
                .andExpect(jsonPath("$[0].atrasada").value(true));
    }

    @Test
    @DisplayName("GET /api/transparencia mostra indicadores gerais e por secretaria")
    void painelDeTransparencia() throws Exception {
        String protocolo = abrirEObterProtocolo(VAZAMENTO);
        mudarStatus(protocolo, """
                {"status":"EM_ATENDIMENTO"}
                """);
        mudarStatus(protocolo, """
                {"status":"RESOLVIDA","observacao":"ok"}
                """);
        salvarAbertaHa("ZEL-ATRASO01", TipoOcorrencia.BURACO_NA_VIA, 80);

        mvc.perform(get("/api/transparencia"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.geral.total").value(2))
                .andExpect(jsonPath("$.geral.emAberto").value(1))
                .andExpect(jsonPath("$.geral.atrasadas").value(1))
                .andExpect(jsonPath("$.geral.percentualResolvidasNoPrazo").value(100.0))
                .andExpect(jsonPath("$.porSecretaria", hasSize(4)))
                .andExpect(jsonPath("$.porSecretaria[0].secretaria").value("Secretaria de Obras"))
                .andExpect(jsonPath("$.porSecretaria[0].indicadores.atrasadas").value(1));
    }

    @Test
    @DisplayName("GET /api/info, /api/tipos e /actuator/health respondem")
    void endpointsPublicos() throws Exception {
        mvc.perform(get("/api/info"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ambiente").value("test"))
                .andExpect(jsonPath("$.versao").value("test"));

        mvc.perform(get("/api/tipos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(6)))
                .andExpect(jsonPath("$[5].codigo").value("VAZAMENTO_DE_AGUA"))
                .andExpect(jsonPath("$[5].prazoHoras").value(24));

        mvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }
}
