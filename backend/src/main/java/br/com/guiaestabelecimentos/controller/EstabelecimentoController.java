package br.com.guiaestabelecimentos.controller;

import br.com.guiaestabelecimentos.model.Estabelecimento;
import br.com.guiaestabelecimentos.service.EstabelecimentoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
@RequestMapping("/api/estabelecimentos")
public class EstabelecimentoController {

    private final EstabelecimentoService service;

    public EstabelecimentoController(EstabelecimentoService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<Estabelecimento> cadastrar(@RequestBody Estabelecimento dados) {
        Estabelecimento criado = service.cadastrar(dados);
        return ResponseEntity.created(URI.create("/api/estabelecimentos/" + criado.getId())).body(criado);
    }

    @GetMapping("/{id}")
    public Estabelecimento consultar(@PathVariable Long id) {
        return service.consultar(id);
    }

    @PutMapping("/{id}")
    public Estabelecimento atualizar(@PathVariable Long id, @RequestBody Estabelecimento dados) {
        return service.atualizar(id, dados);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }
}

