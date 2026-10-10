package br.com.fiap.cidadesesg.dto;

import java.time.Instant;

public record InfoResponse(String aplicacao, String ambiente, String versao, Instant horario) {
}
