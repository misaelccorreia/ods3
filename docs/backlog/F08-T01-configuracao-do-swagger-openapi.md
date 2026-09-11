# F08-T01 — Configuração do Swagger/OpenAPI

**Fase:** Fase 8 — Documentação (Swagger/OpenAPI)
**Complexidade estimada:** P (P=Pequena, M=Média, G=Grande)
**Depende de:** F06-T01, F06-T02, F06-T03, F06-T04, F03-T01
**Bloqueia:** F09-T01
**Referência na spec v1.2:** Seção 19 — Swagger/OpenAPI

## Objetivo

Documentar toda a API no Swagger, incluindo o suporte para autenticar chamadas de endpoints protegidos direto pela interface.

## Descrição

- Configurar springdoc-openapi.
- Declarar o esquema de segurança `bearerAuth` (JWT) na configuração do OpenAPI.
- Conferir que todos os endpoints, DTOs, parâmetros (incluindo os path variables `{nome}` e `{numero}`) e respostas aparecem corretamente na UI do Swagger.

## Entregáveis

- Configuração OpenAPI (`OpenApiConfig` ou equivalente).

## Critérios de aceite

- [ ] É possível fazer login pelo Swagger, colar o token no botão "Authorize" e chamar um endpoint protegido com sucesso direto pela UI.
