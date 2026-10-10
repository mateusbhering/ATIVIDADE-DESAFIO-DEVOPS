package br.com.fiap.cidadesesg.exception;

import br.com.fiap.cidadesesg.model.StatusOcorrencia;

/** Tentativa de pular etapa ou reabrir uma ocorrencia encerrada (HTTP 409). */
public class TransicaoInvalidaException extends RuntimeException {

    public TransicaoInvalidaException(StatusOcorrencia atual, StatusOcorrencia novo) {
        super("Não é possível mudar o status de " + atual + " para " + novo + ".");
    }
}
