package br.com.fiap.cidadesesg.exception;

public class OcorrenciaNaoEncontradaException extends RuntimeException {

    public OcorrenciaNaoEncontradaException(String protocolo) {
        super("Ocorrência " + protocolo + " não encontrada.");
    }
}
