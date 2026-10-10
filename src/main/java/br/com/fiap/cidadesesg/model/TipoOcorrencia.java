package br.com.fiap.cidadesesg.model;

/**
 * Catalogo de problemas que o cidadao pode relatar. Cada tipo define a
 * secretaria responsavel e o prazo de atendimento (em horas, contado da abertura).
 */
public enum TipoOcorrencia {
    BURACO_NA_VIA(Secretaria.OBRAS, 72),
    CALCADA_DANIFICADA(Secretaria.OBRAS, 120),
    ILUMINACAO_APAGADA(Secretaria.SERVICOS_URBANOS, 48),
    DESCARTE_IRREGULAR(Secretaria.MEIO_AMBIENTE, 72),
    PODA_DE_ARVORE(Secretaria.MEIO_AMBIENTE, 96),
    VAZAMENTO_DE_AGUA(Secretaria.SANEAMENTO, 24);

    private final Secretaria secretaria;
    private final int prazoHoras;

    TipoOcorrencia(Secretaria secretaria, int prazoHoras) {
        this.secretaria = secretaria;
        this.prazoHoras = prazoHoras;
    }

    public Secretaria getSecretaria() {
        return secretaria;
    }

    public int getPrazoHoras() {
        return prazoHoras;
    }
}
