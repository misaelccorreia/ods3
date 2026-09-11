package com.projeto.ods3.controller;

import java.net.URI;
import java.util.List;

import com.projeto.ods3.dto.treino.TreinoRequestDTO;
import com.projeto.ods3.dto.treino.TreinoResponseDTO;
import com.projeto.ods3.exception.TreinoNaoEncontradoException;
import com.projeto.ods3.service.TreinoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
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
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

@Tag(name = "Treinos")
@RestController
@RequestMapping("/treinos")
@RequiredArgsConstructor
public class TreinoController {

    private final TreinoService treinoService;

    /** 201 quando o treino é novo; 200 com o existente quando o nome já está cadastrado (Seção 5). */
    @Operation(summary = "Cadastra um treino",
            description = "O autor vem do token. Se já existir um treino com o mesmo nome (comparação exata, "
                    + "case-sensitive), devolve o existente com 200 em vez de duplicar.")
    @ApiResponse(responseCode = "201", description = "Treino criado")
    @ApiResponse(responseCode = "200", description = "Já existia um treino com esse nome; devolvido sem duplicar")
    @ApiResponse(responseCode = "400", description = "Dados inválidos", content = @Content)
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

    @Operation(summary = "Lista todos os treinos, sem paginação")
    @ApiResponse(responseCode = "200", description = "Todos os treinos cadastrados")
    @GetMapping
    public List<TreinoResponseDTO> listar() {
        return treinoService.listar();
    }

    @Operation(summary = "Busca um treino pelo nome")
    @ApiResponse(responseCode = "200", description = "Treino encontrado")
    @ApiResponse(responseCode = "404", description = "Treino não encontrado", content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    @GetMapping("/{nome}")
    public TreinoResponseDTO buscar(
            @Parameter(description = "Nome exato do treino (case-sensitive)", example = "Treino de Perna")
            @PathVariable String nome) {
        return treinoService.buscarPorNome(nome);
    }

    @ExceptionHandler(TreinoNaoEncontradoException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ProblemDetail treinoNaoEncontrado(TreinoNaoEncontradoException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.getMessage());
    }
}
