package com.projeto.ods3.controller;

import com.projeto.ods3.dto.usuario.UsuarioRequestDTO;
import com.projeto.ods3.dto.usuario.UsuarioResponseDTO;
import com.projeto.ods3.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;

    @PostMapping
    public ResponseEntity<UsuarioResponseDTO> cadastrar(@RequestBody @Valid UsuarioRequestDTO dados) {
        return ResponseEntity.status(HttpStatus.CREATED).body(usuarioService.cadastrar(dados));
    }
}
