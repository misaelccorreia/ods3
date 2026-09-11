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
- Criar `UsuarioResponseDTO` (`id`, `nome`, `email`) — **sem** o campo `senha`. `id` é `String` (representação do `_id` gerado pelo MongoDB) — é a única exceção do sistema a expor um id técnico, mantida só como referência de exibição, já que `Usuario` é endereçado por `email` em todos os endpoints (login, token).

## Entregáveis

- Classes `UsuarioRequestDTO` e `UsuarioResponseDTO`.

## Critérios de aceite

- [ ] `UsuarioResponseDTO` não possui, em nenhuma hipótese, um campo de senha ou hash.
- [ ] `id` em `UsuarioResponseDTO` é `String`, não `Long`.
