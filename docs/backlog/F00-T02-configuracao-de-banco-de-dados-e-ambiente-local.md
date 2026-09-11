# F00-T02 — Configuração de banco de dados e ambiente local

**Fase:** Fase 0 — Preparação do Ambiente
**Complexidade estimada:** P (P=Pequena, M=Média, G=Grande)
**Depende de:** F00-T01
**Bloqueia:** F02-T01, F02-T02
**Referência na spec v1.2:** Seção 10 — Banco de dados; Seção 18 — MongoDB

## Objetivo

Disponibilizar um MongoDB acessível localmente e configurar a conexão da aplicação com ele.

## Descrição

- Subir um MongoDB local (docker-compose com serviço `mongo`, ou instância já existente).
- Configurar `application.yml`/`application.properties` com `spring.mongodb.uri` (ou host/porta/database/usuário/senha separados). **Atenção:** em Spring Boot 4 a propriedade `spring.data.mongodb.uri` está deprecada em nível `error` e é ignorada — usar o prefixo `spring.mongodb.*`. Já `spring.data.mongodb.auto-index-creation` continua com o prefixo antigo.
- Não existe conceito de `ddl-auto`/schema/migração — MongoDB é schemaless; coleções e campos aparecem no primeiro `save()`.
- Criar os índices únicos exigidos pela spec assim que as coleções existirem (Seção 18): `email` em `usuarios`, `nome` em `treinos`. Pode ser feito via `@Indexed(unique = true)` nos campos do Model (Fase 1) em vez de script manual — decisão do desenvolvedor.

## Entregáveis

- `docker-compose.yml` (se optado) subindo um container MongoDB.
- `application.yml` com profile de desenvolvimento configurado, apontando para o MongoDB.

## Critérios de aceite

- [ ] A aplicação sobe e conecta ao MongoDB sem erro de conexão.
- [ ] É possível confirmar a conexão (ex.: log do Spring Data MongoDB na inicialização, ou conexão manual via `mongosh`).
