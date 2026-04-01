package br.com.ofisy.interfaces.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;

@RestController
@RequestMapping("/api/teste")
public class OfisyControllerTeste {

    @GetMapping("/hello")
    public ResponseEntity<String> getGreetingMessage() {
        return ResponseEntity.ok("Bem vindo ao Ofisy!");
    }
}