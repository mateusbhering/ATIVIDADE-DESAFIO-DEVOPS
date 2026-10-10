package br.com.fiap.cidadesesg.controller;

import br.com.fiap.cidadesesg.dto.MudancaStatusRequest;
import br.com.fiap.cidadesesg.dto.NovaOcorrenciaRequest;
import br.com.fiap.cidadesesg.dto.OcorrenciaResponse;
import br.com.fiap.cidadesesg.model.Ocorrencia;
import br.com.fiap.cidadesesg.model.StatusOcorrencia;
import br.com.fiap.cidadesesg.model.TipoOcorrencia;
import br.com.fiap.cidadesesg.service.OcorrenciaService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/ocorrencias")
public class OcorrenciaController {

    private final OcorrenciaService service;

    public OcorrenciaController(OcorrenciaService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<OcorrenciaResponse> abrir(@Valid @RequestBody NovaOcorrenciaRequest request) {
        Ocorrencia ocorrencia = service.abrir(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{protocolo}")
                .buildAndExpand(ocorrencia.getProtocolo())
                .toUri();
        return ResponseEntity.created(location).body(resposta(ocorrencia));
    }

    @GetMapping("/{protocolo}")
    public OcorrenciaResponse buscar(@PathVariable String protocolo) {
        return resposta(service.buscar(protocolo));
    }

    @GetMapping
    public List<OcorrenciaResponse> listar(@RequestParam(required = false) StatusOcorrencia status,
                                           @RequestParam(required = false) TipoOcorrencia tipo,
                                           @RequestParam(required = false) String bairro) {
        return respostas(service.listar(status, tipo, bairro));
    }

    @GetMapping("/atrasadas")
    public List<OcorrenciaResponse> atrasadas() {
        return respostas(service.atrasadas());
    }

    @PatchMapping("/{protocolo}/status")
    public OcorrenciaResponse mudarStatus(@PathVariable String protocolo,
                                          @Valid @RequestBody MudancaStatusRequest request) {
        return resposta(service.mudarStatus(protocolo, request));
    }

    private OcorrenciaResponse resposta(Ocorrencia ocorrencia) {
        return OcorrenciaResponse.de(ocorrencia, service.agora());
    }

    private List<OcorrenciaResponse> respostas(List<Ocorrencia> ocorrencias) {
        Instant agora = service.agora();
        return ocorrencias.stream().map(o -> OcorrenciaResponse.de(o, agora)).toList();
    }
}
