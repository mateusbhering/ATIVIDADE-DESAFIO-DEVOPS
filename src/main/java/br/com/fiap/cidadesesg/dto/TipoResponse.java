package br.com.fiap.cidadesesg.dto;

import br.com.fiap.cidadesesg.model.TipoOcorrencia;

public record TipoResponse(TipoOcorrencia codigo, String secretaria, int prazoHoras) {

    public static TipoResponse de(TipoOcorrencia tipo) {
        return new TipoResponse(tipo, tipo.getSecretaria().getNome(), tipo.getPrazoHoras());
    }
}
