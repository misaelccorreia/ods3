package com.projeto.ods3.controller;

import com.projeto.ods3.dto.usuario.UsuarioRequestDTO;
import com.projeto.ods3.dto.usuario.UsuarioResponseDTO;
import com.projeto.ods3.exception.EmailJaCadastradoException;
import com.projeto.ods3.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Usuários")
@SecurityRequirements // público: não exige token
@RestController
@RequestMapping("/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;

    @Operation(summary = "Cadastra um usuário")
    @ApiResponse(responseCode = "201", description = "Usuário criado")
    @ApiResponse(responseCode = "400", description = "Dados inválidos", content = @Content)
    @ApiResponse(responseCode = "409", description = "E-mail já cadastrado", content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UsuarioResponseDTO cadastrar(@RequestBody @Valid UsuarioRequestDTO dados) {
        return usuarioService.cadastrar(dados);
    }

    @ExceptionHandler(EmailJaCadastradoException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ProblemDetail emailJaCadastrado(EmailJaCadastradoException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, e.getMessage());
    }
}
