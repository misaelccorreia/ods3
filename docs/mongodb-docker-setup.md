# MongoDB local — setup via Docker (kernel incompatível com instalação nativa)

Resumo do que foi feito nesta máquina (Ubuntu 24.04, kernel `7.0.0-31-generic`) para disponibilizar um MongoDB local para o projeto. **Isto contradiz e substitui** a decisão registrada em `configurando-ambiente.md` ("MongoDB nativo, sem Docker") — atualizar aquele arquivo.

## Por que não é nativo

O pacote oficial `mongodb-org` (apt, `repo.mongodb.org`) foi instalado, mas o `mongod` se recusa a iniciar em qualquer versão ≥ 8.0 (testado 8.0.30, 8.3.9 e 9.0.1) neste kernel:

```
MongoDB cannot start: Linux kernel versions 6.19 and newer has a known incompatibility with this version of MongoDB.
See https://jira.mongodb.org/browse/SERVER-121912
```

Causa raiz (ver [SERVER-121912](https://jira.mongodb.org/browse/SERVER-121912)): o TCMalloc usado pelo MongoDB 8.0+ viola a ABI do `rseq` em kernels ≥ 6.19 — é uma checagem de segurança real, não um falso positivo, e não tem flag de bypass. O fix (SERVER-125742) só entra em vigor a partir do kernel `7.0.14`; esta máquina está em `7.0.0`. Docker **não contorna isso sozinho** — container Linux compartilha o kernel do host, então `mongod` 8.0+ falha igual dentro de container.

Solução: usar a imagem `mongo:7.0` (MongoDB 7.0.41), que é anterior à mudança de alocador que introduziu o problema e não tem essa checagem. Os pacotes `mongodb-org*` nativos foram removidos do sistema (mantidos apenas os clientes `mongosh` e `mongodb-database-tools`).

## O que está rodando

- Docker Engine instalado (via repo oficial `download.docker.com`). O usuário `misael` **não** tinha sido adicionado ao grupo `docker` (esta linha afirmava o contrário — corrigido em 2026-09-09); foi adicionado depois com `sudo usermod -aG docker misael` + logout/login, e hoje `docker ps` funciona sem `sudo`.
- Container `mongodb`, imagem `mongo:7.0`, porta `27017` publicada em `0.0.0.0:27017`.
- Volume nomeado `mongodb_data` montado em `/data/db` — dados sobrevivem a `docker rm`/restart do container (só se perdem se o volume for removido explicitamente).
- `--restart unless-stopped`: volta sozinho após reboot da máquina (desde que o serviço `docker` suba no boot, que é o padrão).
- **Sem autenticação habilitada** — uso local de desenvolvimento. Não expor a porta 27017 além de `localhost`/rede confiável.

## Connection string

```
mongodb://localhost:27017
```

Para o Spring Boot (`application.properties` / `application.yml`), algo como:

```properties
spring.data.mongodb.uri=mongodb://localhost:27017/<nome-do-banco>
```

(nome do banco ainda não definido na spec — sugestão: `plataforma_treinos`, ajustar conforme o que for decidido no projeto).

## Comandos úteis

```bash
docker ps --filter name=mongodb          # status
docker logs mongodb                      # logs
docker stop mongodb / docker start mongodb
docker restart mongodb
docker exec -it mongodb mongosh          # shell dentro do container
mongosh mongodb://localhost:27017        # shell do host (mongosh já instalado nativamente)
```

## O que um agente/ambiente cloud precisa saber

- Isto é específico do **ambiente local de desenvolvimento** (Ubuntu 24.04, kernel 7.0.0). Em CI/cloud (kernel normalmente diferente/mais antigo, ou gerenciado), `mongodb-org` 8.0+ nativo ou uma imagem `mongo:8.0`/`mongo:latest` oficial devem funcionar normalmente — o problema é específico desta máquina, não do projeto.
- **Não replicar o workaround (`mongo:7.0`) por padrão.** Ele existe só por causa do kernel desta máquina local. No ambiente cloud, tentar primeiro `mongo:8.0`/`mongo:latest` (ou `mongodb-org` nativo) normalmente — só cair para `mongo:7.0` se o mesmo erro aparecer.
- Como confirmar se é o mesmo problema antes de aplicar o workaround:
  1. Rodar `uname -r` no ambiente cloud — o bug afeta kernels `>= 6.19` e `< 7.0.14`.
  2. Olhar o log do `mongod`/container: se aparecer exatamente `"MongoDB cannot start: Linux kernel versions 6.19 and newer has a known incompatibility..."`, é o mesmo caso — trocar para `mongo:7.0` resolve.
  3. Qualquer outro erro de startup **não é este problema** — não aplicar o workaround, investigar a causa normalmente.
- Não assumir MongoDB nativo instalado via apt nesta máquina — foi removido de propósito.
- String de conexão local para testes/dev: `mongodb://localhost:27017`.
