package br.com.fiap.cidadesesg.dto;

import br.com.fiap.cidadesesg.model.TipoOcorrencia;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record NovaOcorrenciaRequest(
        @NotNull(message = "informe o tipo da ocorrência")
        TipoOcorrencia tipo,

        @NotBlank(message = "descreva o problema")
        @Size(max = 500, message = "use no máximo 500 caracteres")
        String descricao,

        @NotBlank(message = "informe o bairro")
        @Size(max = 100, message = "use no máximo 100 caracteres")
        String bairro,

        @NotBlank(message = "informe o endereço")
        @Size(max = 200, message = "use no máximo 200 caracteres")
        String endereco) {
}
