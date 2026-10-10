package br.com.fiap.cidadesesg.dto;

import java.time.Instant;
import java.util.List;

public record PainelTransparencia(Instant atualizadoEm, Indicadores geral, List<IndicadoresSecretaria> porSecretaria) {
}
