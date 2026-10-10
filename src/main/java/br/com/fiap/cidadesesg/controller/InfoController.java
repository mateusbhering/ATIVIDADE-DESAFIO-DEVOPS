package br.com.fiap.cidadesesg.controller;

import br.com.fiap.cidadesesg.dto.InfoResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Clock;
import java.time.Instant;

/** Mostra o ambiente e o commit em execucao; usado pelo smoke test do pipeline. */
@RestController
public class InfoController {

    private final String ambiente;
    private final String versao;
    private final Clock clock;

    public InfoController(@Value("${app.env}") String ambiente,
                          @Value("${app.version}") String versao,
                          Clock clock) {
        this.ambiente = ambiente;
        this.versao = versao;
        this.clock = clock;
    }

    @GetMapping({"/", "/api/info"})
    public InfoResponse info() {
        return new InfoResponse("Cidades ESG Inteligentes - Zeladoria Cidadã", ambiente, versao, Instant.now(clock));
    }
}
