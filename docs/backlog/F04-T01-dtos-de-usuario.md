# F04-T01 — DTOs de Usuario

**Fase:** Fase 4 — Contratos da API (DTOs)
**Complexidade estimada:** P (P=Pequena, M=Média, G=Grande)
**Depende de:** F01-T01
**Bloqueia:** F05-T01
**Referência na spec v1.2:** Seção 16 — DTOs

## Objetivo

Definir o contrato público de cadastro de usuário, garantindo que a senha nunca seja exposta na resposta.

## Descrição

- Criar `UsuarioRequestDTO` (`nome`, `email`, `senha`) com Bean Validation (`@NotBlank`, `@Email`, etc.).
- Criar `UsuarioResponseDTO` (`nome`, `email`) — **sem** o campo `senha` e **sem** `id`. *(Revisado em 2026-09-11: a versão original expunha `id` como "única exceção", o que contradizia a Seção 21 da spec — nenhum endpoint expõe id técnico.)*

## Entregáveis

- Classes `UsuarioRequestDTO` e `UsuarioResponseDTO`.

## Critérios de aceite

- [ ] `UsuarioResponseDTO` não possui, em nenhuma hipótese, um campo de senha ou hash.
- [ ] `UsuarioResponseDTO` não possui campo `id`.
