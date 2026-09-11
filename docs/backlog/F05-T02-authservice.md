# F05-T02 — AuthService

**Fase:** Fase 5 — Regras de Negócio (Service)
**Complexidade estimada:** M (P=Pequena, M=Média, G=Grande)
**Depende de:** F02-T01, F04-T02, F03-T02
**Bloqueia:** F06-T02, F09-T02
**Referência na spec v1.2:** Seção 14 — AuthService

## Objetivo

Autenticar um usuário por email+senha e emitir o token JWT correspondente, com o `email` como claim (não um id técnico).

## Descrição

- Buscar usuário por `email` (`UsuarioRepository.findByEmail`).
- Comparar a senha informada com o hash armazenado via `PasswordEncoder.matches`.
- Em caso de sucesso, gerar o token via `JwtUtil.gerarToken(usuario.getEmail())`; em caso de falha, lançar exceção de credenciais inválidas (ver F07-T01).

## Entregáveis

- Classe `AuthService`.
- Testes unitários: login com credenciais válidas retorna token; login com senha errada ou e-mail inexistente falha.

## Critérios de aceite

- [ ] Login com credenciais corretas retorna um token válido cujo claim `sub` é o email do usuário.
- [ ] Login com credenciais incorretas nunca retorna token.
