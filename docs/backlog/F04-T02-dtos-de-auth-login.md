# F04-T02 — DTOs de Auth (login)

**Fase:** Fase 4 — Contratos da API (DTOs)
**Complexidade estimada:** P (P=Pequena, M=Média, G=Grande)
**Depende de:** F01-T01
**Bloqueia:** F05-T02
**Referência na spec v1.2:** Seção 16 — DTOs

## Objetivo

Definir o contrato de login.

## Descrição

- Criar `LoginRequestDTO` (`email`, `senha`).
- Criar `LoginResponseDTO` (`token`).

## Entregáveis

- Classes `LoginRequestDTO` e `LoginResponseDTO`.

## Critérios de aceite

- [ ] Ambos os DTOs cobrem exatamente os campos definidos na Seção 16, nada além disso.
