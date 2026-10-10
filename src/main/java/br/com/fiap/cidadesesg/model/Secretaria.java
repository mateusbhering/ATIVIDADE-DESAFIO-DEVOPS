package br.com.fiap.cidadesesg.model;

public enum Secretaria {
    OBRAS("Secretaria de Obras"),
    SERVICOS_URBANOS("Secretaria de Serviços Urbanos"),
    MEIO_AMBIENTE("Secretaria do Meio Ambiente"),
    SANEAMENTO("Companhia de Saneamento");

    private final String nome;

    Secretaria(String nome) {
        this.nome = nome;
    }

    public String getNome() {
        return nome;
    }
}
