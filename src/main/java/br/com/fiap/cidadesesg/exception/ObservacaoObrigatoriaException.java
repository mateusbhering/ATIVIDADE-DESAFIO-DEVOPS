package br.com.fiap.cidadesesg.exception;

import br.com.fiap.cidadesesg.model.StatusOcorrencia;

/** Resolver ou cancelar exige uma observacao para a prestacao de contas (HTTP 400). */
public class ObservacaoObrigatoriaException extends RuntimeException {

    public ObservacaoObrigatoriaException(StatusOcorrencia novo) {
        super("Informe uma observação para marcar a ocorrência como " + novo + ".");
    }
}
