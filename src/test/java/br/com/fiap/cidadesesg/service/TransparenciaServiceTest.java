package br.com.fiap.cidadesesg.service;

import br.com.fiap.cidadesesg.dto.Indicadores;
import br.com.fiap.cidadesesg.dto.IndicadoresSecretaria;
import br.com.fiap.cidadesesg.dto.PainelTransparencia;
import br.com.fiap.cidadesesg.model.Ocorrencia;
import br.com.fiap.cidadesesg.model.Secretaria;
import br.com.fiap.cidadesesg.model.StatusOcorrencia;
import br.com.fiap.cidadesesg.model.TipoOcorrencia;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TransparenciaServiceTest {

    private static final Instant AGORA = Instant.parse("2026-03-10T12:00:00Z");

    private static Instant horasAtras(long horas) {
        return AGORA.minus(Duration.ofHours(horas));
    }

    private static Ocorrencia nova(TipoOcorrencia tipo, long abertaHaHoras) {
        return new Ocorrencia("ZEL-" + tipo.ordinal() + abertaHaHoras, tipo, "d", "Centro", "Rua A", horasAtras(abertaHaHoras));
    }

    private static Ocorrencia resolvida(TipoOcorrencia tipo, long abertaHaHoras, long resolvidaHaHoras) {
        Ocorrencia o = nova(tipo, abertaHaHoras);
        o.mudarStatus(StatusOcorrencia.EM_ATENDIMENTO, null, horasAtras(abertaHaHoras - 1));
        o.mudarStatus(StatusOcorrencia.RESOLVIDA, "ok", horasAtras(resolvidaHaHoras));
        return o;
    }

    /** Mesmo cenario dos dados de demonstracao. */
    private static List<Ocorrencia> cenario() {
        Ocorrencia emAtendimento = nova(TipoOcorrencia.ILUMINACAO_APAGADA, 30);
        emAtendimento.mudarStatus(StatusOcorrencia.EM_ATENDIMENTO, null, horasAtras(6));
        Ocorrencia cancelada = nova(TipoOcorrencia.CALCADA_DANIFICADA, 50);
        cancelada.mudarStatus(StatusOcorrencia.CANCELADA, "Duplicada", horasAtras(40));

        return List.of(
                resolvida(TipoOcorrencia.VAZAMENTO_DE_AGUA, 20, 10),  // 10 h, prazo 24 h: no prazo
                resolvida(TipoOcorrencia.BURACO_NA_VIA, 120, 24),     // 96 h, prazo 72 h: fora do prazo
                emAtendimento,
                nova(TipoOcorrencia.DESCARTE_IRREGULAR, 100),          // 100 h, prazo 72 h: atrasada
                nova(TipoOcorrencia.PODA_DE_ARVORE, 2),
                cancelada);
    }

    @Test
    @DisplayName("painel geral conta total, em aberto, atrasadas, resolvidas e canceladas")
    void contagemGeral() {
        Indicadores geral = TransparenciaService.calcular(cenario(), AGORA).geral();

        assertThat(geral.total()).isEqualTo(6);
        assertThat(geral.emAberto()).isEqualTo(3);
        assertThat(geral.atrasadas()).isEqualTo(1);
        assertThat(geral.resolvidas()).isEqualTo(2);
        assertThat(geral.canceladas()).isEqualTo(1);
    }

    @Test
    @DisplayName("percentual resolvido no prazo e tempo médio de resolução em horas")
    void percentualNoPrazoETempoMedio() {
        Indicadores geral = TransparenciaService.calcular(cenario(), AGORA).geral();

        assertThat(geral.resolvidasNoPrazo()).isEqualTo(1);
        assertThat(geral.percentualResolvidasNoPrazo()).isEqualTo(50.0);
        assertThat(geral.tempoMedioResolucaoHoras()).isEqualTo(53.0); // (10 + 96) / 2
    }

    @Test
    @DisplayName("painel por secretaria lista todas as secretarias, inclusive sem ocorrências")
    void indicadoresPorSecretaria() {
        PainelTransparencia painel = TransparenciaService.calcular(
                List.of(resolvida(TipoOcorrencia.VAZAMENTO_DE_AGUA, 20, 10)), AGORA);

        assertThat(painel.porSecretaria()).extracting(IndicadoresSecretaria::secretaria)
                .containsExactly(Secretaria.OBRAS.getNome(), Secretaria.SERVICOS_URBANOS.getNome(),
                        Secretaria.MEIO_AMBIENTE.getNome(), Secretaria.SANEAMENTO.getNome());

        Indicadores saneamento = painel.porSecretaria().get(3).indicadores();
        assertThat(saneamento.total()).isEqualTo(1);
        assertThat(saneamento.percentualResolvidasNoPrazo()).isEqualTo(100.0);

        Indicadores obras = painel.porSecretaria().get(0).indicadores();
        assertThat(obras.total()).isZero();
        assertThat(obras.percentualResolvidasNoPrazo()).isNull();
        assertThat(obras.tempoMedioResolucaoHoras()).isNull();
    }
}
