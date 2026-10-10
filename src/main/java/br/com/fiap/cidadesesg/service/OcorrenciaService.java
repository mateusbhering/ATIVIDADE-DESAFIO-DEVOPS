package br.com.fiap.cidadesesg.service;

import br.com.fiap.cidadesesg.dto.MudancaStatusRequest;
import br.com.fiap.cidadesesg.dto.NovaOcorrenciaRequest;
import br.com.fiap.cidadesesg.exception.OcorrenciaNaoEncontradaException;
import br.com.fiap.cidadesesg.model.Ocorrencia;
import br.com.fiap.cidadesesg.model.StatusOcorrencia;
import br.com.fiap.cidadesesg.model.TipoOcorrencia;
import br.com.fiap.cidadesesg.repository.OcorrenciaRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Clock;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

@Service
@Transactional(readOnly = true)
public class OcorrenciaService {

    // sem 0/O e 1/I para o cidadao nao confundir ao digitar o protocolo
    private static final String ALFABETO_PROTOCOLO = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final int TAMANHO_PROTOCOLO = 8;

    private final OcorrenciaRepository repository;
    private final Clock clock;
    private final SecureRandom random = new SecureRandom();

    public OcorrenciaService(OcorrenciaRepository repository, Clock clock) {
        this.repository = repository;
        this.clock = clock;
    }

    @Transactional
    public Ocorrencia abrir(NovaOcorrenciaRequest request) {
        Ocorrencia ocorrencia = new Ocorrencia(
                gerarProtocolo(),
                request.tipo(),
                request.descricao().trim(),
                request.bairro().trim(),
                request.endereco().trim(),
                agora());
        return repository.save(ocorrencia);
    }

    public Ocorrencia buscar(String protocolo) {
        return repository.findByProtocolo(protocolo.trim().toUpperCase(Locale.ROOT))
                .orElseThrow(() -> new OcorrenciaNaoEncontradaException(protocolo));
    }

    /** Lista as ocorrencias mais recentes primeiro; filtros nulos sao ignorados. */
    public List<Ocorrencia> listar(StatusOcorrencia status, TipoOcorrencia tipo, String bairro) {
        return repository.findAll(Sort.by(Sort.Direction.DESC, "abertaEm")).stream()
                .filter(o -> status == null || o.getStatus() == status)
                .filter(o -> tipo == null || o.getTipo() == tipo)
                .filter(o -> bairro == null || bairro.isBlank() || o.getBairro().equalsIgnoreCase(bairro.trim()))
                .toList();
    }

    /** Pendentes com prazo vencido, das mais atrasadas (prazo mais antigo) para as menos. */
    public List<Ocorrencia> atrasadas() {
        Instant agora = agora();
        return repository.findByStatusIn(List.of(StatusOcorrencia.ABERTA, StatusOcorrencia.EM_ATENDIMENTO)).stream()
                .filter(o -> o.isAtrasada(agora))
                .sorted(Comparator.comparing(Ocorrencia::getPrazoLimite))
                .toList();
    }

    @Transactional
    public Ocorrencia mudarStatus(String protocolo, MudancaStatusRequest request) {
        Ocorrencia ocorrencia = buscar(protocolo);
        ocorrencia.mudarStatus(request.status(), request.observacao(), agora());
        return ocorrencia;
    }

    public Instant agora() {
        return Instant.now(clock);
    }

    private String gerarProtocolo() {
        String protocolo;
        do {
            StringBuilder sb = new StringBuilder("ZEL-");
            for (int i = 0; i < TAMANHO_PROTOCOLO; i++) {
                sb.append(ALFABETO_PROTOCOLO.charAt(random.nextInt(ALFABETO_PROTOCOLO.length())));
            }
            protocolo = sb.toString();
        } while (repository.existsByProtocolo(protocolo));
        return protocolo;
    }
}
