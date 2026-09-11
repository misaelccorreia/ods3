package com.projeto.ods3.service;

import java.util.Comparator;
import java.util.List;
import java.util.function.Supplier;

import com.projeto.ods3.dto.comentario.ComentarioRequestDTO;
import com.projeto.ods3.dto.comentario.ComentarioResponseDTO;
import com.projeto.ods3.exception.ComentarioNaoEncontradoException;
import com.projeto.ods3.exception.TreinoNaoEncontradoException;
import com.projeto.ods3.model.Comentario;
import com.projeto.ods3.model.TipoAvaliacao;
import com.projeto.ods3.model.Treino;
import com.projeto.ods3.model.Usuario;
import com.projeto.ods3.repository.TreinoRepository;
import com.projeto.ods3.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;

/**
 * Comentários e avaliações vivem dentro do documento Treino, então toda escrita é
 * carregar o treino, alterar a lista embutida e salvar o treino inteiro de volta.
 */
@Service
@RequiredArgsConstructor
public class ComentarioService {

    static final int TENTATIVAS = 3;

    private static final Comparator<Comentario> MAIOR_SCORE_PRIMEIRO =
            Comparator.comparingInt(Comentario::calcularScore).reversed()
                    .thenComparingInt(Comentario::getNumero);

    private final TreinoRepository treinoRepository;
    private final UsuarioRepository usuarioRepository;

    public ComentarioResponseDTO comentar(String nomeTreino, ComentarioRequestDTO dados, String emailAutor) {
        Usuario autor = usuarioRepository.findByEmail(emailAutor)
                .orElseThrow(() -> new IllegalStateException("Usuário autenticado não encontrado: " + emailAutor));

        return comRetentativa(() -> {
            Treino treino = buscarTreino(nomeTreino);
            int numero = treino.getComentarios().stream().mapToInt(Comentario::getNumero).max().orElse(0) + 1;
            Comentario comentario = new Comentario(numero, dados.texto(), autor.getNome(), autor.getEmail());
            treino.getComentarios().add(comentario);
            treinoRepository.save(treino);
            return ComentarioResponseDTO.de(comentario);
        });
    }

    public ComentarioResponseDTO avaliar(String nomeTreino, int numero, String emailUsuario, TipoAvaliacao tipo) {
        return comRetentativa(() -> {
            Treino treino = buscarTreino(nomeTreino);
            Comentario comentario = treino.getComentarios().stream()
                    .filter(c -> c.getNumero() == numero)
                    .findFirst()
                    .orElseThrow(() -> new ComentarioNaoEncontradoException(nomeTreino, numero));
            comentario.registrarAvaliacao(emailUsuario, tipo);
            treinoRepository.save(treino);
            return ComentarioResponseDTO.de(comentario);
        });
    }

    /** Ordenação em memória, por score decrescente; empates pelo número. Não altera o número de ninguém. */
    public List<ComentarioResponseDTO> listar(String nomeTreino) {
        return buscarTreino(nomeTreino).getComentarios().stream()
                .sorted(MAIOR_SCORE_PRIMEIRO)
                .map(ComentarioResponseDTO::de)
                .toList();
    }

    private Treino buscarTreino(String nome) {
        return treinoRepository.findByNome(nome).orElseThrow(() -> new TreinoNaoEncontradoException(nome));
    }

    /**
     * O Treino tem @Version: se outra requisição salvou o mesmo treino entre a leitura e a gravação,
     * o save falha em vez de sobrescrever o comentário ou voto alheio. Relê e tenta de novo.
     */
    private <T> T comRetentativa(Supplier<T> operacao) {
        for (int tentativa = 1; ; tentativa++) {
            try {
                return operacao.get();
            } catch (OptimisticLockingFailureException e) {
                if (tentativa == TENTATIVAS) {
                    throw e;
                }
            }
        }
    }
}
