package com.projeto.ods3.service;

import java.util.List;
import java.util.Optional;

import com.projeto.ods3.dto.treino.TreinoRequestDTO;
import com.projeto.ods3.dto.treino.TreinoResponseDTO;
import com.projeto.ods3.exception.TreinoNaoEncontradoException;
import com.projeto.ods3.model.Treino;
import com.projeto.ods3.model.Usuario;
import com.projeto.ods3.repository.TreinoRepository;
import com.projeto.ods3.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TreinoService {

    private final TreinoRepository treinoRepository;
    private final UsuarioRepository usuarioRepository;

    /** O treino e se ele foi criado agora — o Controller decide 201 ou 200 com base nisso. */
    public record Cadastro(TreinoResponseDTO treino, boolean criado) {
    }

    public Cadastro cadastrar(TreinoRequestDTO dados, String emailAutor) {
        Optional<Treino> existente = treinoRepository.findByNome(dados.nome());
        if (existente.isPresent()) {
            return new Cadastro(TreinoResponseDTO.de(existente.get()), false);
        }

        Usuario autor = usuarioRepository.findByEmail(emailAutor)
                .orElseThrow(() -> new IllegalStateException("Usuário autenticado não encontrado: " + emailAutor));
        Treino treino = new Treino(dados.nome(), dados.descricao(), autor.getNome(), autor.getEmail());

        try {
            return new Cadastro(TreinoResponseDTO.de(treinoRepository.save(treino)), true);
        } catch (DuplicateKeyException e) {
            // cadastro simultâneo do mesmo nome: devolve o que foi gravado primeiro, como se já existisse
            return new Cadastro(TreinoResponseDTO.de(buscar(dados.nome())), false);
        }
    }

    public List<TreinoResponseDTO> listar() {
        return treinoRepository.findAll().stream().map(TreinoResponseDTO::de).toList();
    }

    public TreinoResponseDTO buscarPorNome(String nome) {
        return TreinoResponseDTO.de(buscar(nome));
    }

    private Treino buscar(String nome) {
        return treinoRepository.findByNome(nome).orElseThrow(() -> new TreinoNaoEncontradoException(nome));
    }
}
