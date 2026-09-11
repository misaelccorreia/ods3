# F05-T03 — TreinoService

**Fase:** Fase 5 — Regras de Negócio (Service)
**Complexidade estimada:** M (P=Pequena, M=Média, G=Grande)
**Depende de:** F02-T02, F04-T03
**Bloqueia:** F06-T03, F09-T02
**Referência na spec v1.2:** Seção 5 — Regra de negócio; Seção 14 — TreinoService

## Objetivo

Implementar a regra de não-duplicidade de treino e a criação/consulta de treinos, endereçando sempre por `nome` — nunca por id técnico.

## Descrição

- Antes de criar, verificar `existsByNome`/`findByNome`; se existir, retornar o `TreinoResponseDTO` do treino já existente (HTTP 200 é decidido no Controller, não aqui).
- Se não existir, criar o treino associando `autorNome`/`autorEmail` a partir do usuário autenticado (email recebido via parâmetro vindo do Controller/SecurityContext; nome resolvido com `UsuarioRepository.findByEmail`).
- Implementar consulta de todos os treinos e de **um treino específico por `nome`** (`GET /treinos/{nome}`) — sem paginação.

## Entregáveis

- Classe `TreinoService`.
- Testes unitários: treino existente é retornado sem duplicar; treino inexistente é criado; nome com diferença de caixa é tratado como treino diferente; consulta por `nome` inexistente lança exceção de não encontrado (ver F07-T01).

## Critérios de aceite

- [ ] Cadastrar duas vezes o mesmo nome exato não gera dois registros no banco.
- [ ] Cadastrar `"Treino de Perna"` e depois `"treino de perna"` gera dois registros distintos (case-sensitive confirmado).
- [ ] Nenhum método público do Service recebe ou retorna um id técnico do MongoDB — só `nome`.
