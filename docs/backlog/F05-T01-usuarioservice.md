# F05-T01 — UsuarioService

**Fase:** Fase 5 — Regras de Negócio (Service)
**Complexidade estimada:** M (P=Pequena, M=Média, G=Grande)
**Depende de:** F02-T01, F04-T01, F03-T01
**Bloqueia:** F06-T01, F09-T02
**Referência na spec v1.2:** Seção 14 — UsuarioService

## Objetivo

Implementar o cadastro de usuário com hash de senha e validação de e-mail único.

## Descrição

- Validar, antes de salvar, que o e-mail ainda não está cadastrado (`existsByEmail`) — lançar exceção de negócio caso já exista (ver F07-T01).
- Aplicar `PasswordEncoder.encode(senha)` antes de persistir.
- Converter `UsuarioRequestDTO` → `Usuario` → `UsuarioResponseDTO` (o `id` do documento salvo, gerado pelo MongoDB, entra no DTO de resposta como `String`).

## Entregáveis

- Classe `UsuarioService`.
- Testes unitários (JUnit + Mockito): cadastro com sucesso, tentativa de cadastro com e-mail já existente, senha nunca armazenada em texto plano.

## Critérios de aceite

- [ ] Duas tentativas de cadastro com o mesmo e-mail resultam em erro na segunda.
- [ ] A senha persistida no banco nunca é igual à senha enviada em texto plano.
