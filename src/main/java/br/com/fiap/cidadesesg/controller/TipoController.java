package br.com.fiap.cidadesesg.controller;

import br.com.fiap.cidadesesg.dto.TipoResponse;
import br.com.fiap.cidadesesg.model.TipoOcorrencia;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

@RestController
public class TipoController {

    @GetMapping("/api/tipos")
    public List<TipoResponse> tipos() {
        return Arrays.stream(TipoOcorrencia.values()).map(TipoResponse::de).toList();
    }
}
