package br.com.fiap.cidadesesg.controller;

import br.com.fiap.cidadesesg.dto.PainelTransparencia;
import br.com.fiap.cidadesesg.service.TransparenciaService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TransparenciaController {

    private final TransparenciaService service;

    public TransparenciaController(TransparenciaService service) {
        this.service = service;
    }

    @GetMapping("/api/transparencia")
    public PainelTransparencia painel() {
        return service.painel();
    }
}
