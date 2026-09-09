/**
 * Camada de regras de negócio — principal ponto de concentração da lógica da aplicação.
 *
 * <p>Comportamento próprio do domínio (registrar avaliação, calcular score) fica no Model,
 * não aqui. Não há {@code ComentarioService} apoiado em repositório próprio: comentário é
 * estrutura embutida em {@code Treino} e é persistido através do {@code TreinoRepository}.
 */
package com.projeto.ods3.service;
