package br.com.fiap.cidadesesg.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TipoOcorrenciaTest {

    @Test
    @DisplayName("cada tipo tem a secretaria e o prazo definidos nas regras de negócio")
    void secretariaEPrazoPorTipo() {
        assertThat(TipoOcorrencia.BURACO_NA_VIA).extracting("secretaria", "prazoHoras").containsExactly(Secretaria.OBRAS, 72);
        assertThat(TipoOcorrencia.CALCADA_DANIFICADA).extracting("secretaria", "prazoHoras").containsExactly(Secretaria.OBRAS, 120);
        assertThat(TipoOcorrencia.ILUMINACAO_APAGADA).extracting("secretaria", "prazoHoras").containsExactly(Secretaria.SERVICOS_URBANOS, 48);
        assertThat(TipoOcorrencia.DESCARTE_IRREGULAR).extracting("secretaria", "prazoHoras").containsExactly(Secretaria.MEIO_AMBIENTE, 72);
        assertThat(TipoOcorrencia.PODA_DE_ARVORE).extracting("secretaria", "prazoHoras").containsExactly(Secretaria.MEIO_AMBIENTE, 96);
        assertThat(TipoOcorrencia.VAZAMENTO_DE_AGUA).extracting("secretaria", "prazoHoras").containsExactly(Secretaria.SANEAMENTO, 24);
    }
}
