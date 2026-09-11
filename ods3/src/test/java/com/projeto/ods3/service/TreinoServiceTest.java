package com.projeto.ods3.service;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import com.projeto.ods3.dto.treino.TreinoRequestDTO;
import com.projeto.ods3.dto.treino.TreinoResponseDTO;
import com.projeto.ods3.exception.TreinoNaoEncontradoException;
import com.projeto.ods3.model.Treino;
import com.projeto.ods3.model.Usuario;
import com.projeto.ods3.repository.TreinoRepository;
import com.projeto.ods3.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DuplicateKeyException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TreinoServiceTest {

    @Mock TreinoRepository treinoRepository;
    @Mock UsuarioRepository usuarioRepository;
    @InjectMocks TreinoService service;

    private static final Usuario ANA = new Usuario("Ana", "ana@exemplo.com", "hash");

    @Test
    void treinoExistenteEhRetornadoSemDuplicar() {
        Treino existente = new Treino("Treino de Perna", "original", "Bruno", "bruno@exemplo.com");
        when(treinoRepository.findByNome("Treino de Perna")).thenReturn(Optional.of(existente));

        TreinoService.Cadastro cadastro = service.cadastrar(new TreinoRequestDTO("Treino de Perna", "outra"), "ana@exemplo.com");

        assertFalse(cadastro.criado());
        assertEquals("original", cadastro.treino().descricao());
        assertEquals("Bruno", cadastro.treino().autorNome());
        verify(treinoRepository, never()).save(any());
    }

    @Test
    void treinoInexistenteEhCriadoComAutorDoToken() {
        when(treinoRepository.findByNome("Treino de Perna")).thenReturn(Optional.empty());
        when(usuarioRepository.findByEmail("ana@exemplo.com")).thenReturn(Optional.of(ANA));
        when(treinoRepository.save(any(Treino.class))).thenAnswer(inv -> inv.getArgument(0));

        TreinoService.Cadastro cadastro = service.cadastrar(new TreinoRequestDTO("Treino de Perna", "desc"), "ana@exemplo.com");

        assertTrue(cadastro.criado());
        assertEquals("Ana", cadastro.treino().autorNome());
        assertEquals("ana@exemplo.com", cadastro.treino().autorEmail());
        assertNotNull(cadastro.treino().dataCriacao());
    }

    @Test
    void nomeComCaixaDiferenteEhOutroTreino() {
        when(treinoRepository.findByNome("treino de perna")).thenReturn(Optional.empty());
        when(usuarioRepository.findByEmail("ana@exemplo.com")).thenReturn(Optional.of(ANA));
        when(treinoRepository.save(any(Treino.class))).thenAnswer(inv -> inv.getArgument(0));

        TreinoService.Cadastro cadastro = service.cadastrar(new TreinoRequestDTO("treino de perna", "desc"), "ana@exemplo.com");

        ArgumentCaptor<Treino> salvo = ArgumentCaptor.forClass(Treino.class);
        verify(treinoRepository).save(salvo.capture());
        assertTrue(cadastro.criado());
        assertEquals("treino de perna", salvo.getValue().getNome(), "o nome nao pode ser normalizado");
        verify(treinoRepository, never()).findByNome("Treino de Perna");
    }

    @Test
    void cadastroSimultaneoDoMesmoNomeDevolveOQueFoiGravado() {
        Treino vencedor = new Treino("Treino de Perna", "gravado antes", "Bruno", "bruno@exemplo.com");
        when(treinoRepository.findByNome("Treino de Perna")).thenReturn(Optional.empty(), Optional.of(vencedor));
        when(usuarioRepository.findByEmail("ana@exemplo.com")).thenReturn(Optional.of(ANA));
        when(treinoRepository.save(any(Treino.class))).thenThrow(new DuplicateKeyException("E11000"));

        TreinoService.Cadastro cadastro = service.cadastrar(new TreinoRequestDTO("Treino de Perna", "desc"), "ana@exemplo.com");

        assertFalse(cadastro.criado());
        assertEquals("gravado antes", cadastro.treino().descricao());
    }

    @Test
    void buscaPorNomeExistente() {
        when(treinoRepository.findByNome("Treino de Perna"))
                .thenReturn(Optional.of(new Treino("Treino de Perna", "desc", "Ana", "ana@exemplo.com")));

        assertEquals("Treino de Perna", service.buscarPorNome("Treino de Perna").nome());
    }

    @Test
    void buscaPorNomeInexistenteLancaNaoEncontrado() {
        when(treinoRepository.findByNome("Inexistente")).thenReturn(Optional.empty());

        TreinoNaoEncontradoException erro = assertThrows(TreinoNaoEncontradoException.class,
                () -> service.buscarPorNome("Inexistente"));
        assertTrue(erro.getMessage().contains("Inexistente"));
    }

    @Test
    void listaTodosSemPaginacao() {
        when(treinoRepository.findAll()).thenReturn(List.of(
                new Treino("A", "a", "Ana", "ana@exemplo.com"),
                new Treino("B", "b", "Ana", "ana@exemplo.com")));

        assertEquals(List.of("A", "B"), service.listar().stream().map(TreinoResponseDTO::nome).toList());
    }

    @Test
    void nenhumMetodoPublicoExpoeModelOuId() {
        for (Method m : TreinoService.class.getDeclaredMethods()) {
            if (!Modifier.isPublic(m.getModifiers())) continue;
            assertFalse(m.getName().toLowerCase().endsWith("id"), m.getName());
            assertFalse(m.getReturnType().getPackageName().endsWith(".model"), m.getName() + " devolve um model");
            assertTrue(Arrays.stream(m.getParameterTypes()).noneMatch(p -> p.getPackageName().endsWith(".model")),
                    m.getName() + " recebe um model");
        }
    }
}
