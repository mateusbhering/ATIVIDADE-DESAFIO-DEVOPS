package br.com.fiap.cidadesesg.config;

import br.com.fiap.cidadesesg.model.Ocorrencia;
import br.com.fiap.cidadesesg.model.StatusOcorrencia;
import br.com.fiap.cidadesesg.model.TipoOcorrencia;
import br.com.fiap.cidadesesg.repository.OcorrenciaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

/**
 * Com APP_SEED=true e o banco vazio, cria 6 ocorrencias ficticias para o painel
 * de transparencia ja comecar com dados (local e staging).
 */
@Component
@ConditionalOnProperty(name = "app.seed", havingValue = "true")
public class DadosDemonstracao implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DadosDemonstracao.class);

    private final OcorrenciaRepository repository;
    private final Clock clock;

    public DadosDemonstracao(OcorrenciaRepository repository, Clock clock) {
        this.repository = repository;
        this.clock = clock;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (repository.count() > 0) {
            return;
        }
        Instant agora = Instant.now(clock);

        // resolvida no prazo: 10 h para um prazo de 24 h
        Ocorrencia vazamento = nova("ZEL-DEMO0001", TipoOcorrencia.VAZAMENTO_DE_AGUA,
                "Água escorrendo pela sarjeta há dois dias", "Vila Nova", "Rua Sete, 88", agora, 20);
        vazamento.mudarStatus(StatusOcorrencia.EM_ATENDIMENTO, null, horasAtras(agora, 18));
        vazamento.mudarStatus(StatusOcorrencia.RESOLVIDA, "Tubulação substituída", horasAtras(agora, 10));

        // resolvida fora do prazo: 96 h para um prazo de 72 h
        Ocorrencia buraco = nova("ZEL-DEMO0002", TipoOcorrencia.BURACO_NA_VIA,
                "Buraco grande na faixa da direita", "Centro", "Av. Brasil, 1200", agora, 120);
        buraco.mudarStatus(StatusOcorrencia.EM_ATENDIMENTO, null, horasAtras(agora, 100));
        buraco.mudarStatus(StatusOcorrencia.RESOLVIDA, "Asfalto recomposto", horasAtras(agora, 24));

        // em atendimento, dentro do prazo de 48 h
        Ocorrencia poste = nova("ZEL-DEMO0003", TipoOcorrencia.ILUMINACAO_APAGADA,
                "Poste apagado em frente à escola", "Jardim das Flores", "Rua das Acácias, 45", agora, 30);
        poste.mudarStatus(StatusOcorrencia.EM_ATENDIMENTO, "Equipe agendada", horasAtras(agora, 6));

        // atrasada: aberta há 100 h com prazo de 72 h
        Ocorrencia descarte = nova("ZEL-DEMO0004", TipoOcorrencia.DESCARTE_IRREGULAR,
                "Entulho e móveis descartados na calçada", "Vila Nova", "Rua Quatro, 10", agora, 100);

        // recente
        Ocorrencia poda = nova("ZEL-DEMO0005", TipoOcorrencia.PODA_DE_ARVORE,
                "Galhos encostando na fiação", "Centro", "Praça da Matriz, s/n", agora, 2);

        // cancelada
        Ocorrencia calcada = nova("ZEL-DEMO0006", TipoOcorrencia.CALCADA_DANIFICADA,
                "Calçada quebrada", "Jardim das Flores", "Rua das Acácias, 50", agora, 50);
        calcada.mudarStatus(StatusOcorrencia.CANCELADA, "Duplicada da ocorrência ZEL-DEMO0003 (mesmo endereço)",
                horasAtras(agora, 40));

        repository.saveAll(List.of(vazamento, buraco, poste, descarte, poda, calcada));
        log.info("Dados de demonstração criados: 6 ocorrências (ZEL-DEMO0001 a ZEL-DEMO0006)");
    }

    private static Ocorrencia nova(String protocolo, TipoOcorrencia tipo, String descricao,
                                   String bairro, String endereco, Instant agora, long horasAtras) {
        return new Ocorrencia(protocolo, tipo, descricao, bairro, endereco, horasAtras(agora, horasAtras));
    }

    private static Instant horasAtras(Instant agora, long horas) {
        return agora.minus(Duration.ofHours(horas));
    }
}
