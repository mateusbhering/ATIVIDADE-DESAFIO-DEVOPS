package br.com.fiap.cidadesesg.model;

/**
 * Fluxo de atendimento:
 * ABERTA -> EM_ATENDIMENTO -> RESOLVIDA, com CANCELADA possivel nas duas primeiras etapas.
 * RESOLVIDA e CANCELADA sao estados finais.
 */
public enum StatusOcorrencia {
    ABERTA,
    EM_ATENDIMENTO,
    RESOLVIDA,
    CANCELADA;

    public boolean isPendente() {
        return this == ABERTA || this == EM_ATENDIMENTO;
    }

    public boolean isEncerrada() {
        return !isPendente();
    }

    public boolean podeMudarPara(StatusOcorrencia novo) {
        return switch (this) {
            case ABERTA -> novo == EM_ATENDIMENTO || novo == CANCELADA;
            case EM_ATENDIMENTO -> novo == RESOLVIDA || novo == CANCELADA;
            case RESOLVIDA, CANCELADA -> false;
        };
    }
}
