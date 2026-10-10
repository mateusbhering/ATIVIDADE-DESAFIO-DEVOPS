package br.com.fiap.cidadesesg.dto;

import br.com.fiap.cidadesesg.model.Ocorrencia;
import br.com.fiap.cidadesesg.model.StatusOcorrencia;
import br.com.fiap.cidadesesg.model.TipoOcorrencia;

import java.time.Instant;

public record OcorrenciaResponse(
        String protocolo,
        TipoOcorrencia tipo,
        String secretaria,
        String descricao,
        String bairro,
        String endereco,
        StatusOcorrencia status,
        Instant abertaEm,
        Instant prazoLimite,
        Instant atendimentoIniciadoEm,
        Instant encerradaEm,
        String observacao,
        boolean atrasada) {

    public static OcorrenciaResponse de(Ocorrencia o, Instant agora) {
        return new OcorrenciaResponse(
                o.getProtocolo(),
                o.getTipo(),
                o.getSecretaria().getNome(),
                o.getDescricao(),
                o.getBairro(),
                o.getEndereco(),
                o.getStatus(),
                o.getAbertaEm(),
                o.getPrazoLimite(),
                o.getAtendimentoIniciadoEm(),
                o.getEncerradaEm(),
                o.getObservacao(),
                o.isAtrasada(agora));
    }
}
