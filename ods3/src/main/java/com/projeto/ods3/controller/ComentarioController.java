package com.projeto.ods3.controller;

import java.util.List;

import com.projeto.ods3.dto.comentario.ComentarioRequestDTO;
import com.projeto.ods3.dto.comentario.ComentarioResponseDTO;
import com.projeto.ods3.model.TipoAvaliacao;
import com.projeto.ods3.service.ComentarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Comentário não é recurso próprio: é sempre endereçado pelo nome do treino e pelo número dentro dele. */
@RestController
@RequestMapping("/treinos/{nome}/comentarios")
@RequiredArgsConstructor
public class ComentarioController {

    private final ComentarioService comentarioService;

    @PostMapping
    public ResponseEntity<ComentarioResponseDTO> comentar(@PathVariable String nome,
                                                          @RequestBody @Valid ComentarioRequestDTO dados,
                                                          Authentication autenticado) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(comentarioService.comentar(nome, dados, autenticado.getName()));
    }

    /** Já ordenada por score decrescente. */
    @GetMapping
    public List<ComentarioResponseDTO> listar(@PathVariable String nome) {
        return comentarioService.listar(nome);
    }

    @PostMapping("/{numero}/like")
    public ComentarioResponseDTO like(@PathVariable String nome, @PathVariable int numero,
                                      Authentication autenticado) {
        return comentarioService.avaliar(nome, numero, autenticado.getName(), TipoAvaliacao.LIKE);
    }

    @PostMapping("/{numero}/dislike")
    public ComentarioResponseDTO dislike(@PathVariable String nome, @PathVariable int numero,
                                         Authentication autenticado) {
        return comentarioService.avaliar(nome, numero, autenticado.getName(), TipoAvaliacao.DISLIKE);
    }
}
