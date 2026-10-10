package br.com.fiap.cidadesesg.dto;

/**
 * Indicadores do painel de transparencia.
 * percentualResolvidasNoPrazo e tempoMedioResolucaoHoras ficam nulos enquanto nao houver ocorrencias resolvidas.
 */
public record Indicadores(
        long total,
        long emAberto,
        long atrasadas,
        long resolvidas,
        long canceladas,
        long resolvidasNoPrazo,
        Double percentualResolvidasNoPrazo,
        Double tempoMedioResolucaoHoras) {
}
