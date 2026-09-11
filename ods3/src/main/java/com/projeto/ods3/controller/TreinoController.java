package com.projeto.ods3.controller;

import java.net.URI;
import java.util.List;

import com.projeto.ods3.dto.treino.TreinoRequestDTO;
import com.projeto.ods3.dto.treino.TreinoResponseDTO;
import com.projeto.ods3.exception.TreinoNaoEncontradoException;
import com.projeto.ods3.service.TreinoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequestMapping("/treinos")
@RequiredArgsConstructor
public class TreinoController {

    private final TreinoService treinoService;

    /** 201 quando o treino é novo; 200 com o existente quando o nome já está cadastrado (Seção 5). */
    @PostMapping
    public ResponseEntity<TreinoResponseDTO> cadastrar(@RequestBody @Valid TreinoRequestDTO dados,
                                                       Authentication autenticado) {
        TreinoService.Cadastro cadastro = treinoService.cadastrar(dados, autenticado.getName());
        if (!cadastro.criado()) {
            return ResponseEntity.ok(cadastro.treino());
        }

        URI local = UriComponentsBuilder.fromPath("/treinos/{nome}").encode()
                .buildAndExpand(cadastro.treino().nome()).toUri();
        return ResponseEntity.created(local).body(cadastro.treino());
    }

    @GetMapping
    public List<TreinoResponseDTO> listar() {
        return treinoService.listar();
    }

    @GetMapping("/{nome}")
    public TreinoResponseDTO buscar(@PathVariable String nome) {
        return treinoService.buscarPorNome(nome);
    }

    @ExceptionHandler(TreinoNaoEncontradoException.class)
    public ProblemDetail treinoNaoEncontrado(TreinoNaoEncontradoException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.getMessage());
    }
}
