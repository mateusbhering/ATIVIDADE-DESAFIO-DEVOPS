package br.com.fiap.cidadesesg.service;

import br.com.fiap.cidadesesg.dto.Indicadores;
import br.com.fiap.cidadesesg.dto.IndicadoresSecretaria;
import br.com.fiap.cidadesesg.dto.PainelTransparencia;
import br.com.fiap.cidadesesg.model.Ocorrencia;
import br.com.fiap.cidadesesg.model.Secretaria;
import br.com.fiap.cidadesesg.model.StatusOcorrencia;
import br.com.fiap.cidadesesg.repository.OcorrenciaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.OptionalDouble;

/** Painel publico de desempenho: geral e por secretaria. */
@Service
public class TransparenciaService {

    private final OcorrenciaRepository repository;
    private final Clock clock;

    public TransparenciaService(OcorrenciaRepository repository, Clock clock) {
        this.repository = repository;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public PainelTransparencia painel() {
        return calcular(repository.findAll(), Instant.now(clock));
    }

    /** Calculo puro (sem banco), testado diretamente nos testes unitarios. */
    public static PainelTransparencia calcular(List<Ocorrencia> ocorrencias, Instant agora) {
        List<IndicadoresSecretaria> porSecretaria = Arrays.stream(Secretaria.values())
                .map(secretaria -> new IndicadoresSecretaria(
                        secretaria.getNome(),
                        indicadores(ocorrencias.stream().filter(o -> o.getSecretaria() == secretaria).toList(), agora)))
                .toList();
        return new PainelTransparencia(agora, indicadores(ocorrencias, agora), porSecretaria);
    }

    private static Indicadores indicadores(List<Ocorrencia> ocorrencias, Instant agora) {
        long emAberto = ocorrencias.stream().filter(o -> o.getStatus().isPendente()).count();
        long atrasadas = ocorrencias.stream().filter(o -> o.isAtrasada(agora)).count();
        long resolvidas = contar(ocorrencias, StatusOcorrencia.RESOLVIDA);
        long canceladas = contar(ocorrencias, StatusOcorrencia.CANCELADA);
        long noPrazo = ocorrencias.stream().filter(Ocorrencia::isResolvidaNoPrazo).count();

        Double percentual = resolvidas == 0 ? null : arredondar(100.0 * noPrazo / resolvidas);
        OptionalDouble mediaMinutos = ocorrencias.stream()
                .map(Ocorrencia::getTempoResolucao)
                .filter(Objects::nonNull)
                .mapToLong(Duration::toMinutes)
                .average();
        Double tempoMedioHoras = mediaMinutos.isPresent() ? arredondar(mediaMinutos.getAsDouble() / 60.0) : null;

        return new Indicadores(ocorrencias.size(), emAberto, atrasadas, resolvidas, canceladas,
                noPrazo, percentual, tempoMedioHoras);
    }

    private static long contar(List<Ocorrencia> ocorrencias, StatusOcorrencia status) {
        return ocorrencias.stream().filter(o -> o.getStatus() == status).count();
    }

    private static double arredondar(double valor) {
        return Math.round(valor * 10.0) / 10.0;
    }
}
