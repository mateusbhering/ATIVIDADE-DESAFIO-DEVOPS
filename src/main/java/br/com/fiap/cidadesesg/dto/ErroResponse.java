package br.com.fiap.cidadesesg.dto;

import org.springframework.http.HttpStatus;

import java.time.Instant;

public record ErroResponse(int status, String erro, String mensagem, Instant horario) {

    public static ErroResponse of(HttpStatus status, String mensagem) {
        return new ErroResponse(status.value(), status.getReasonPhrase(), mensagem, Instant.now());
    }
}
