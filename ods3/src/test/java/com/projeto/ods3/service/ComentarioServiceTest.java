package com.projeto.ods3.service;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.OptimisticLockingFailureException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ComentarioServiceTest {

    @Mock TreinoRepository treinoRepository;
    @Mock UsuarioRepository usuarioRepository;
    @InjectMocks ComentarioService service;

    private static final String PERNA = "Treino de Perna";

    private static Treino treino(String nome) {
        return new Treino(nome, "desc", "Ana", "ana@exemplo.com");
    }

    private void autor(String email, String nome) {
        when(usuarioRepository.findByEmail(email)).thenReturn(Optional.of(new Usuario(nome, email, "hash")));
    }

    private static Comentario comentarioComScore(int numero, int likes, int dislikes) {
        Comentario c = new Comentario(numero, "texto " + numero, "Ana", "ana@exemplo.com");
        for (int i = 0; i < likes; i++) c.registrarAvaliacao("like" + i + "@exemplo.com", TipoAvaliacao.LIKE);
        for (int i = 0; i < dislikes; i++) c.registrarAvaliacao("dislike" + i + "@exemplo.com", TipoAvaliacao.DISLIKE);
        return c;
    }

    // ---------------- comentar ----------------

    @Test
    void numeroEhSequencialComecandoEmUm() {
        Treino perna = treino(PERNA);
        autor("bruno@exemplo.com", "Bruno");
        when(treinoRepository.findByNome(PERNA)).thenReturn(Optional.of(perna));

        int primeiro = service.comentar(PERNA, new ComentarioRequestDTO("um"), "bruno@exemplo.com").numero();
        int segundo = service.comentar(PERNA, new ComentarioRequestDTO("dois"), "bruno@exemplo.com").numero();
        int terceiro = service.comentar(PERNA, new ComentarioRequestDTO("tres"), "bruno@exemplo.com").numero();

        assertEquals(List.of(1, 2, 3), List.of(primeiro, segundo, terceiro));
        assertEquals(3, perna.getComentarios().size());
        verify(treinoRepository, times(3)).save(perna);
    }

    @Test
    void numeroEhMaiorExistenteMaisUmNaoTamanhoDaLista() {
        Treino perna = treino(PERNA);
        perna.getComentarios().add(new Comentario(1, "a", "Ana", "ana@exemplo.com"));
        perna.getComentarios().add(new Comentario(5, "b", "Ana", "ana@exemplo.com"));
        autor("bruno@exemplo.com", "Bruno");
        when(treinoRepository.findByNome(PERNA)).thenReturn(Optional.of(perna));

        assertEquals(6, service.comentar(PERNA, new ComentarioRequestDTO("c"), "bruno@exemplo.com").numero());
    }

    @Test
    void numeroEhEscopadoPorTreino() {
        autor("bruno@exemplo.com", "Bruno");
        when(treinoRepository.findByNome("Perna")).thenReturn(Optional.of(treino("Perna")));
        when(treinoRepository.findByNome("Costas")).thenReturn(Optional.of(treino("Costas")));

        assertEquals(1, service.comentar("Perna", new ComentarioRequestDTO("x"), "bruno@exemplo.com").numero());
        assertEquals(1, service.comentar("Costas", new ComentarioRequestDTO("y"), "bruno@exemplo.com").numero());
    }

    @Test
    void comentarioLevaAutorDoToken() {
        autor("bruno@exemplo.com", "Bruno");
        when(treinoRepository.findByNome(PERNA)).thenReturn(Optional.of(treino(PERNA)));

        ComentarioResponseDTO dto = service.comentar(PERNA, new ComentarioRequestDTO("bom"), "bruno@exemplo.com");

        assertEquals("Bruno", dto.autorNome());
        assertEquals("bruno@exemplo.com", dto.autorEmail());
        assertEquals("bom", dto.texto());
    }

    @Test
    void comentarEmTreinoInexistenteLancaTreinoNaoEncontrado() {
        autor("bruno@exemplo.com", "Bruno");
        when(treinoRepository.findByNome("Nada")).thenReturn(Optional.empty());

        assertThrows(TreinoNaoEncontradoException.class,
                () -> service.comentar("Nada", new ComentarioRequestDTO("x"), "bruno@exemplo.com"));
        verify(treinoRepository, never()).save(any());
    }

    // ---------------- avaliar ----------------

    @Test
    void likeEmComentarioSemAvaliacaoPrevia() {
        Treino perna = treino(PERNA);
        perna.getComentarios().add(new Comentario(1, "a", "Ana", "ana@exemplo.com"));
        when(treinoRepository.findByNome(PERNA)).thenReturn(Optional.of(perna));

        ComentarioResponseDTO dto = service.avaliar(PERNA, 1, "carla@exemplo.com", TipoAvaliacao.LIKE);

        assertEquals(1, dto.likes());
        assertEquals(0, dto.dislikes());
        verify(treinoRepository).save(perna);
    }

    @Test
    void trocaDeLikeParaDislikeSubstituiNaoAcumula() {
        Treino perna = treino(PERNA);
        perna.getComentarios().add(new Comentario(1, "a", "Ana", "ana@exemplo.com"));
        when(treinoRepository.findByNome(PERNA)).thenReturn(Optional.of(perna));

        service.avaliar(PERNA, 1, "carla@exemplo.com", TipoAvaliacao.LIKE);
        ComentarioResponseDTO dto = service.avaliar(PERNA, 1, "carla@exemplo.com", TipoAvaliacao.DISLIKE);

        assertEquals(0, dto.likes());
        assertEquals(1, dto.dislikes());
        assertEquals(-1, dto.score());
    }

    @Test
    void trocaDeDislikeParaLikeSubstituiNaoAcumula() {
        Treino perna = treino(PERNA);
        perna.getComentarios().add(new Comentario(1, "a", "Ana", "ana@exemplo.com"));
        when(treinoRepository.findByNome(PERNA)).thenReturn(Optional.of(perna));

        service.avaliar(PERNA, 1, "carla@exemplo.com", TipoAvaliacao.DISLIKE);
        ComentarioResponseDTO dto = service.avaliar(PERNA, 1, "carla@exemplo.com", TipoAvaliacao.LIKE);

        assertEquals(1, dto.likes());
        assertEquals(0, dto.dislikes());
        assertEquals(1, dto.score());
    }

    @Test
    void avaliarNumeroInexistenteLancaComentarioNaoEncontrado() {
        Treino perna = treino(PERNA);
        perna.getComentarios().add(new Comentario(1, "a", "Ana", "ana@exemplo.com"));
        when(treinoRepository.findByNome(PERNA)).thenReturn(Optional.of(perna));

        ComentarioNaoEncontradoException erro = assertThrows(ComentarioNaoEncontradoException.class,
                () -> service.avaliar(PERNA, 99, "carla@exemplo.com", TipoAvaliacao.LIKE));
        assertTrue(erro.getMessage().contains("99") && erro.getMessage().contains(PERNA), erro.getMessage());
        verify(treinoRepository, never()).save(any());
    }

    @Test
    void avaliarEmTreinoInexistenteLancaTreinoNaoEncontrado() {
        when(treinoRepository.findByNome("Nada")).thenReturn(Optional.empty());

        assertThrows(TreinoNaoEncontradoException.class,
                () -> service.avaliar("Nada", 1, "carla@exemplo.com", TipoAvaliacao.LIKE));
    }

    // ---------------- listar ----------------

    @Test
    void listaOrdenadaPorScoreDecrescenteComEmpatesENumeroEstavel() {
        Treino perna = treino(PERNA);
        perna.getComentarios().add(comentarioComScore(1, 0, 0));  // score 0
        perna.getComentarios().add(comentarioComScore(2, 3, 1));  // score 2
        perna.getComentarios().add(comentarioComScore(3, 2, 0));  // score 2 (empate com o 2)
        perna.getComentarios().add(comentarioComScore(4, 0, 1));  // score -1
        when(treinoRepository.findByNome(PERNA)).thenReturn(Optional.of(perna));

        List<ComentarioResponseDTO> lista = service.listar(PERNA);

        assertEquals(List.of(2, 3, 1, 4), lista.stream().map(ComentarioResponseDTO::numero).toList());
        assertEquals(List.of(2, 2, 0, -1), lista.stream().map(ComentarioResponseDTO::score).toList());
        assertEquals(List.of(1, 2, 3, 4), perna.getComentarios().stream().map(Comentario::getNumero).toList(),
                "ordenar a resposta nao pode reordenar nem renumerar o documento");
    }

    @Test
    void listarDeTreinoInexistenteLancaTreinoNaoEncontrado() {
        when(treinoRepository.findByNome("Nada")).thenReturn(Optional.empty());

        assertThrows(TreinoNaoEncontradoException.class, () -> service.listar("Nada"));
    }

    // ---------------- concorrência ----------------

    @Test
    void conflitoDeVersaoRelêOTreinoETentaDeNovo() {
        autor("bruno@exemplo.com", "Bruno");
        // cada leitura devolve um documento novo, como o banco faria
        when(treinoRepository.findByNome(PERNA)).thenAnswer(inv -> Optional.of(treino(PERNA)));
        when(treinoRepository.save(any(Treino.class)))
                .thenThrow(new OptimisticLockingFailureException("versao mudou"))
                .thenAnswer(inv -> inv.getArgument(0));

        ComentarioResponseDTO dto = service.comentar(PERNA, new ComentarioRequestDTO("x"), "bruno@exemplo.com");

        assertEquals(1, dto.numero());
        verify(treinoRepository, times(2)).findByNome(PERNA);
    }

    @Test
    void conflitoPersistenteDesisteAposAsTentativas() {
        when(treinoRepository.findByNome(PERNA)).thenAnswer(inv -> {
            Treino t = treino(PERNA);
            t.getComentarios().add(new Comentario(1, "a", "Ana", "ana@exemplo.com"));
            return Optional.of(t);
        });
        when(treinoRepository.save(any(Treino.class))).thenThrow(new OptimisticLockingFailureException("versao mudou"));

        assertThrows(OptimisticLockingFailureException.class,
                () -> service.avaliar(PERNA, 1, "carla@exemplo.com", TipoAvaliacao.LIKE));
        verify(treinoRepository, times(ComentarioService.TENTATIVAS)).save(any());
    }

    @Test
    void nenhumMetodoPublicoExpoeModelOuId() {
        for (Method m : ComentarioService.class.getDeclaredMethods()) {
            if (!Modifier.isPublic(m.getModifiers())) continue;
            assertFalse(m.getName().toLowerCase().endsWith("id"), m.getName());
            assertFalse(m.getReturnType().getPackageName().endsWith(".model"), m.getName() + " devolve um model");
            assertTrue(Arrays.stream(m.getParameterTypes())
                            .filter(p -> p != TipoAvaliacao.class)
                            .noneMatch(p -> p.getPackageName().endsWith(".model")),
                    m.getName() + " recebe um model");
        }
    }
}
