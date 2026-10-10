package br.com.fiap.cidadesesg.service;

import br.com.fiap.cidadesesg.dto.NovaOcorrenciaRequest;
import br.com.fiap.cidadesesg.model.Ocorrencia;
import br.com.fiap.cidadesesg.model.StatusOcorrencia;
import br.com.fiap.cidadesesg.model.TipoOcorrencia;
import br.com.fiap.cidadesesg.repository.OcorrenciaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class OcorrenciaServiceTest {

    @Test
    @DisplayName("abrir gera protocolo ZEL-XXXXXXXX, limpa os campos e usa a data do relógio")
    void abrirGeraProtocoloEUsaDataDoRelogio() {
        Instant agora = Instant.parse("2026-03-02T12:00:00Z");
        OcorrenciaRepository repository = mock(OcorrenciaRepository.class);
        when(repository.existsByProtocolo(anyString())).thenReturn(false);
        when(repository.save(any(Ocorrencia.class))).thenAnswer(inv -> inv.getArgument(0));
        OcorrenciaService service = new OcorrenciaService(repository, Clock.fixed(agora, ZoneOffset.UTC));

        Ocorrencia o = service.abrir(new NovaOcorrenciaRequest(
                TipoOcorrencia.VAZAMENTO_DE_AGUA, " Água na sarjeta ", " Vila Nova ", "Rua Sete, 88"));

        assertThat(o.getProtocolo()).matches("ZEL-[A-Z2-9]{8}");
        assertThat(o.getStatus()).isEqualTo(StatusOcorrencia.ABERTA);
        assertThat(o.getAbertaEm()).isEqualTo(agora);
        assertThat(o.getDescricao()).isEqualTo("Água na sarjeta");
        assertThat(o.getBairro()).isEqualTo("Vila Nova");
        assertThat(o.getPrazoLimite()).isEqualTo(Instant.parse("2026-03-03T12:00:00Z"));
    }
}
