package br.com.guiaestabelecimentos.controller;

import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.net.URI;

@Controller
public class PaginaInicialController {

    @GetMapping("/")
    public ResponseEntity<Void> abrirInicio() {
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create("/index.html"))
                .cacheControl(CacheControl.noStore())
                .build();
    }
}
