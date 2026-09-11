package com.projeto.ods3.controller;

import java.util.List;

import com.projeto.ods3.dto.comentario.ComentarioRequestDTO;
import com.projeto.ods3.dto.comentario.ComentarioResponseDTO;
import com.projeto.ods3.exception.ComentarioNaoEncontradoException;
import com.projeto.ods3.exception.TreinoNaoEncontradoException;
import com.projeto.ods3.model.TipoAvaliacao;
import com.projeto.ods3.service.ComentarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** Comentário não é recurso próprio: é sempre endereçado pelo nome do treino e pelo número dentro dele. */
@Tag(name = "Comentários e avaliações")
@RestController
@RequestMapping("/treinos/{nome}/comentarios")
@RequiredArgsConstructor
public class ComentarioController {

    private final ComentarioService comentarioService;

    @Operation(summary = "Comenta um treino",
            description = "O autor vem do token. O número é sequencial dentro do treino e não muda depois.")
    @ApiResponse(responseCode = "201", description = "Comentário criado")
    @ApiResponse(responseCode = "400", description = "Dados inválidos", content = @Content)
    @ApiResponse(responseCode = "404", description = "Treino não encontrado", content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "409", description = "Treino alterado por outra requisição ao mesmo tempo", content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ComentarioResponseDTO comentar(@Parameter(description = "Nome exato do treino", example = "Treino de Perna")
                                          @PathVariable String nome,
                                          @RequestBody @Valid ComentarioRequestDTO dados,
                                          Authentication autenticado) {
        return comentarioService.comentar(nome, dados, autenticado.getName());
    }

    @Operation(summary = "Lista os comentários do treino, do maior para o menor score")
    @ApiResponse(responseCode = "200", description = "Comentários ordenados por score decrescente")
    @ApiResponse(responseCode = "404", description = "Treino não encontrado", content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    @GetMapping
    public List<ComentarioResponseDTO> listar(@Parameter(description = "Nome exato do treino", example = "Treino de Perna")
                                              @PathVariable String nome) {
        return comentarioService.listar(nome);
    }

    @Operation(summary = "Dá like no comentário", description = "Substitui o voto anterior do mesmo usuário, se houver.")
    @ApiResponse(responseCode = "200", description = "Voto registrado")
    @ApiResponse(responseCode = "404", description = "Treino ou comentário não encontrado", content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "409", description = "Treino alterado por outra requisição ao mesmo tempo", content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    @PostMapping("/{numero}/like")
    public ComentarioResponseDTO like(@Parameter(description = "Nome exato do treino", example = "Treino de Perna")
                                      @PathVariable String nome,
                                      @Parameter(description = "Número do comentário dentro do treino", example = "1")
                                      @PathVariable int numero,
                                      Authentication autenticado) {
        return comentarioService.avaliar(nome, numero, autenticado.getName(), TipoAvaliacao.LIKE);
    }

    @Operation(summary = "Dá dislike no comentário", description = "Substitui o voto anterior do mesmo usuário, se houver.")
    @ApiResponse(responseCode = "200", description = "Voto registrado")
    @ApiResponse(responseCode = "404", description = "Treino ou comentário não encontrado", content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "409", description = "Treino alterado por outra requisição ao mesmo tempo", content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    @PostMapping("/{numero}/dislike")
    public ComentarioResponseDTO dislike(@Parameter(description = "Nome exato do treino", example = "Treino de Perna")
                                         @PathVariable String nome,
                                         @Parameter(description = "Número do comentário dentro do treino", example = "1")
                                         @PathVariable int numero,
                                         Authentication autenticado) {
        return comentarioService.avaliar(nome, numero, autenticado.getName(), TipoAvaliacao.DISLIKE);
    }

    /** A mensagem diz se o que faltou foi o treino ou o comentário dentro dele. */
    @ExceptionHandler({TreinoNaoEncontradoException.class, ComentarioNaoEncontradoException.class})
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ProblemDetail naoEncontrado(RuntimeException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.getMessage());
    }

    /** Só chega aqui se o treino foi alterado por outras requisições em todas as tentativas do Service. */
    @ExceptionHandler(OptimisticLockingFailureException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ProblemDetail conflitoDeGravacao(OptimisticLockingFailureException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT,
                "O treino foi alterado por outra requisição ao mesmo tempo; tente novamente");
    }
}
