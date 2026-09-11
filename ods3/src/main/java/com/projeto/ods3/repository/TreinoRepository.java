package com.projeto.ods3.repository;

import java.util.Optional;

import com.projeto.ods3.model.Treino;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * Único caminho de persistência de Treino e, por consequência, dos comentários embutidos nele.
 * As buscas por nome são exatas e case-sensitive (Seção 5 da spec).
 */
public interface TreinoRepository extends MongoRepository<Treino, String> {

    boolean existsByNome(String nome);

    Optional<Treino> findByNome(String nome);
}
