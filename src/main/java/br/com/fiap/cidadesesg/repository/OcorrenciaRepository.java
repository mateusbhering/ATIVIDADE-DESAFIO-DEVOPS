package br.com.fiap.cidadesesg.repository;

import br.com.fiap.cidadesesg.model.Ocorrencia;
import br.com.fiap.cidadesesg.model.StatusOcorrencia;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface OcorrenciaRepository extends JpaRepository<Ocorrencia, Long> {

    Optional<Ocorrencia> findByProtocolo(String protocolo);

    boolean existsByProtocolo(String protocolo);

    List<Ocorrencia> findByStatusIn(Collection<StatusOcorrencia> status);
}
