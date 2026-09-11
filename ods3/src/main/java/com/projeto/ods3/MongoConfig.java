package com.projeto.ods3;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;

/**
 * Ajuste de mapeamento do Spring Data MongoDB.
 *
 * <p>As avaliações de um comentário são um {@code Map} cuja chave é o e-mail de quem votou
 * (Seção 7.2 da spec), e o MongoDB não aceita ponto em nome de campo de sub-documento.
 * Sem esta configuração, gravar qualquer avaliação falha com
 * "Map key ... contains dots but no replacement was configured".
 *
 * <p>O Spring Data troca o ponto por {@value #SUBSTITUTO_DO_PONTO} ao gravar e desfaz a troca
 * ao ler, então em Java a chave continua sendo o e-mail real. A sequência foi escolhida por
 * não ser válida em endereço de e-mail: se fosse algo como "_", um e-mail como
 * {@code joao_silva@gmail.com} voltaria do banco corrompido como {@code joao.silva@gmail.com}.
 */
@Configuration
public class MongoConfig {

    /** Sequência gravada no lugar do ponto nas chaves de mapa. */
    public static final String SUBSTITUTO_DO_PONTO = "[dot]";

    @Autowired
    void configurarEscapeDePontoEmChavesDeMapa(MappingMongoConverter converter) {
        converter.setMapKeyDotReplacement(SUBSTITUTO_DO_PONTO);
    }
}
