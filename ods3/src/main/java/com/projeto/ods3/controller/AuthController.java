package com.projeto.ods3.controller;

import com.projeto.ods3.dto.auth.LoginRequestDTO;
import com.projeto.ods3.dto.auth.LoginResponseDTO;
import com.projeto.ods3.exception.CredenciaisInvalidasException;
import com.projeto.ods3.service.AuthService;
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
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Autenticação")
@SecurityRequirements // público: não exige token
@RestController
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "Autentica com e-mail e senha e devolve o token JWT",
            description = "Copie o token da resposta e cole em \"Authorize\" para chamar os demais endpoints.")
    @ApiResponse(responseCode = "200", description = "Token emitido")
    @ApiResponse(responseCode = "400", description = "Dados inválidos", content = @Content)
    @ApiResponse(responseCode = "401", description = "E-mail ou senha inválidos", content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    @PostMapping("/login")
    public LoginResponseDTO login(@RequestBody @Valid LoginRequestDTO dados) {
        return authService.login(dados);
    }

    @ExceptionHandler(CredenciaisInvalidasException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ProblemDetail credenciaisInvalidas(CredenciaisInvalidasException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, e.getMessage());
    }
}
