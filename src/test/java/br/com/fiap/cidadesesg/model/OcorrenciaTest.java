package br.com.fiap.cidadesesg.model;

import br.com.fiap.cidadesesg.exception.ObservacaoObrigatoriaException;
import br.com.fiap.cidadesesg.exception.TransicaoInvalidaException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OcorrenciaTest {

    private static final Instant ABERTURA = Instant.parse("2026-03-02T12:00:00Z");

    private static Ocorrencia buraco() {
        return new Ocorrencia("ZEL-TESTE001", TipoOcorrencia.BURACO_NA_VIA, "Buraco", "Centro", "Rua A, 1", ABERTURA);
    }

    private static Instant horasDepois(long horas) {
        return ABERTURA.plus(Duration.ofHours(horas));
    }

    @Test
    @DisplayName("nova ocorrência começa ABERTA e o prazo é a abertura + prazo do tipo")
    void novaOcorrenciaComecaAbertaComPrazoDoTipo() {
        Ocorrencia o = buraco();

        assertThat(o.getStatus()).isEqualTo(StatusOcorrencia.ABERTA);
        assertThat(o.getSecretaria()).isEqualTo(Secretaria.OBRAS);
        assertThat(o.getPrazoLimite()).isEqualTo(horasDepois(72));
    }

    @Test
    @DisplayName("fluxo completo registra datas de atendimento, encerramento e a observação")
    void fluxoCompletoRegistraDatasEObservacao() {
        Ocorrencia o = buraco();

        o.mudarStatus(StatusOcorrencia.EM_ATENDIMENTO, null, horasDepois(5));
        o.mudarStatus(StatusOcorrencia.RESOLVIDA, "  Asfalto recomposto ", horasDepois(30));

        assertThat(o.getStatus()).isEqualTo(StatusOcorrencia.RESOLVIDA);
        assertThat(o.getAtendimentoIniciadoEm()).isEqualTo(horasDepois(5));
        assertThat(o.getEncerradaEm()).isEqualTo(horasDepois(30));
        assertThat(o.getObservacao()).isEqualTo("Asfalto recomposto");
        assertThat(o.getTempoResolucao()).isEqualTo(Duration.ofHours(30));
    }

    @Test
    @DisplayName("não é possível pular de ABERTA direto para RESOLVIDA")
    void naoPermitePularEtapa() {
        Ocorrencia o = buraco();

        assertThatThrownBy(() -> o.mudarStatus(StatusOcorrencia.RESOLVIDA, "ok", horasDepois(1)))
                .isInstanceOf(TransicaoInvalidaException.class);
        assertThat(o.getStatus()).isEqualTo(StatusOcorrencia.ABERTA);
    }

    @Test
    @DisplayName("ocorrência encerrada não pode ser reaberta")
    void naoPermiteReabrirEncerrada() {
        Ocorrencia o = buraco();
        o.mudarStatus(StatusOcorrencia.CANCELADA, "Duplicada", horasDepois(1));

        assertThatThrownBy(() -> o.mudarStatus(StatusOcorrencia.EM_ATENDIMENTO, null, horasDepois(2)))
                .isInstanceOf(TransicaoInvalidaException.class);
        assertThat(o.getStatus()).isEqualTo(StatusOcorrencia.CANCELADA);
    }

    @Test
    @DisplayName("cancelar ou resolver sem observação é recusado")
    void encerrarExigeObservacao() {
        Ocorrencia o = buraco();

        assertThatThrownBy(() -> o.mudarStatus(StatusOcorrencia.CANCELADA, "   ", horasDepois(1)))
                .isInstanceOf(ObservacaoObrigatoriaException.class);
        assertThat(o.getStatus()).isEqualTo(StatusOcorrencia.ABERTA);
        assertThat(o.getEncerradaEm()).isNull();
    }

    @Test
    @DisplayName("pendente fica atrasada somente depois do prazo")
    void pendenteFicaAtrasadaDepoisDoPrazo() {
        Ocorrencia o = buraco();

        assertThat(o.isAtrasada(horasDepois(72))).isFalse();
        assertThat(o.isAtrasada(horasDepois(73))).isTrue();

        o.mudarStatus(StatusOcorrencia.EM_ATENDIMENTO, null, horasDepois(10));
        assertThat(o.isAtrasada(horasDepois(73))).isTrue();
    }

    @Test
    @DisplayName("ocorrência encerrada nunca aparece como atrasada")
    void encerradaNaoFicaAtrasada() {
        Ocorrencia o = buraco();
        o.mudarStatus(StatusOcorrencia.CANCELADA, "Duplicada", horasDepois(1));

        assertThat(o.isAtrasada(horasDepois(500))).isFalse();
    }

    @Test
    @DisplayName("resolvida no prazo x resolvida fora do prazo")
    void resolvidaNoPrazoOuForaDoPrazo() {
        Ocorrencia noPrazo = buraco();
        noPrazo.mudarStatus(StatusOcorrencia.EM_ATENDIMENTO, null, horasDepois(1));
        noPrazo.mudarStatus(StatusOcorrencia.RESOLVIDA, "ok", horasDepois(72));

        Ocorrencia foraDoPrazo = buraco();
        foraDoPrazo.mudarStatus(StatusOcorrencia.EM_ATENDIMENTO, null, horasDepois(1));
        foraDoPrazo.mudarStatus(StatusOcorrencia.RESOLVIDA, "ok", horasDepois(80));

        assertThat(noPrazo.isResolvidaNoPrazo()).isTrue();
        assertThat(foraDoPrazo.isResolvidaNoPrazo()).isFalse();
    }
}
