package br.com.fiap.cidadesesg.dto;

import br.com.fiap.cidadesesg.model.StatusOcorrencia;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record MudancaStatusRequest(
        @NotNull(message = "informe o novo status")
        StatusOcorrencia status,

        @Size(max = 500, message = "use no máximo 500 caracteres")
        String observacao) {
}
