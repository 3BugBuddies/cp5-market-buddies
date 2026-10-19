# MarketBuddies — CheckPoint 5 | Java Advanced

Mercado express com loja web autenticada (Spring Security + JWT + Thymeleaf + PostgreSQL) e pagamento assíncrono pelo **Tranquilo Bank** via RabbitMQ, com e-mail de confirmação e dashboard da gerência. Entrega única das **Partes I e II** do CheckPoint 5 da disciplina de Java Advanced (TDS) — FIAP.

- **Parte I:** loja web com landing, login e cadastro próprios, perfis CUSTOMER e ADMIN, CRUD de produtos pelo ADMIN e página 403 estilizada, tudo no PostgreSQL.
- **Parte II:** carrinho, checkout, pedido de pagamento publicado no RabbitMQ, integração com o Tranquilo Bank, resposta assíncrona, e-mail de confirmação registrado no banco e dashboard da gerência.

> Rascunho em construção. Os trechos marcados com _a preencher_ e os comentários `print pendente` entram quando o sistema estiver rodando.

---

## Deploy

| Recurso | URL |
|---------|-----|
| Aplicação — MarketBuddies API (Railway) | https://marketbuddies-api-production.up.railway.app |
| Login | https://marketbuddies-api-production.up.railway.app/login |
| Tranquilo Bank (API de pagamentos) | https://tranquilo-bank-production.up.railway.app |
| Painel do RabbitMQ | https://rabbitmq-production-e3dc.up.railway.app |
| Vídeo | _a preencher_ |

> O deploy roda no plano de teste do Railway, que tem **crédito limitado**. Os serviços ficam no ar até aproximadamente _a preencher após a medição de consumo_ (data). Depois disso, a aplicação pode ficar indisponível; o vídeo e os prints deste README mostram o fluxo completo.

<!-- print pendente: assets/railway-servicos.png -->
<!-- print pendente: assets/railway-deploy.png -->

---

## Integrantes do Grupo

| Nome | RM |
|------|----|
| Felipe Yuiti Ishii | 565339 |
| Gabriel Nogueira Peixoto | 563925 |
| Giovanna Neri dos Santos | 566154 |
| Mariana Inoue | 565834 |

**Turma:** 2TDSR
**IDE utilizada:** IntelliJ IDEA

---

## Arquitetura

![Arquitetura do MarketBuddies](assets/arquitetura.png)

---

## Configuração do Projeto — Spring Initializr

Spring Boot 4.1.1, Java 21, Maven, configuração em YAML. Cada serviço foi gerado separadamente no Initializr.

### MarketBuddies API

![Spring Initializr — MarketBuddies API](assets/spring-initializr-marketbuddies-api.png)

| Dependência | Categoria | Descrição |
|-------------|-----------|-----------|
| Spring Web | WEB | Camada MVC (controllers, dispatcher servlet) |
| Spring Security | SECURITY | Autenticação, perfis e autorização por rota |
| Thymeleaf | TEMPLATE ENGINES | Telas renderizadas no servidor |
| Spring Data JPA | SQL | Persistência com JPA e Hibernate |
| PostgreSQL Driver | SQL | Driver JDBC do PostgreSQL |
| Validation | I/O | Bean Validation nos formulários e nos corpos da API |
| Lombok | DEVELOPER TOOLS | Redução de boilerplate |
| Spring Boot DevTools | DEVELOPER TOOLS | Reinício automático em desenvolvimento |
| Spring for RabbitMQ | MESSAGING | Envio do pedido de pagamento e leitura das respostas |
| Java Mail Sender | I/O | E-mail de confirmação de pagamento |

Dependências que entram no `pom.xml` fora do Initializr:

| Dependência | Para quê |
|-------------|----------|
| `thymeleaf-extras-springsecurity6` | O Initializr inclui sozinho quando Security e Thymeleaf estão marcados; mostra ou esconde elementos por perfil (`sec:authorize`) |
| `jjwt` (0.12.x) | Gera e valida o token JWT. Entra com a tarefa de segurança (T4). A demo da Aula 13 usa a 0.11.5, cuja API está descontinuada |

### MarketBuddies Worker

![Spring Initializr — MarketBuddies Worker](assets/spring-initializr-marketbuddies-worker.png)

| Dependência | Categoria | Descrição |
|-------------|-----------|-----------|
| Spring for RabbitMQ | MESSAGING | Consome os pedidos e publica as respostas |
| HTTP Client | WEB | `RestClient` para chamar o Tranquilo Bank, sem subir servidor web |
| Lombok | DEVELOPER TOOLS | Redução de boilerplate |
| Spring Boot DevTools | DEVELOPER TOOLS | Reinício automático em desenvolvimento |

### Tranquilo Bank

![Spring Initializr — Tranquilo Bank](assets/spring-initializr-tranquilo-bank.png)

| Dependência | Categoria | Descrição |
|-------------|-----------|-----------|
| Spring Web | WEB | API REST de pagamentos |
| Validation | I/O | Validação da requisição de autorização |
| Lombok | DEVELOPER TOOLS | Redução de boilerplate |
| Spring Boot DevTools | DEVELOPER TOOLS | Reinício automático em desenvolvimento |

---

## Estrutura do Projeto

Pacotes no plural, por camada, no padrão dos projetos do professor.

```
cp5-market-buddies/
├── marketbuddies-api/        # loja web, API REST e mensageria (porta 8080)
│   └── src/main/
│       ├── java/.../api/
│       │   ├── configs/      # segurança, RabbitMQ e dados iniciais
│       │   ├── controllers/  # telas e /api/**
│       │   ├── services/     # regras de negócio
│       │   ├── facades/      # fluxo do pagamento
│       │   ├── producers/    # publica o pedido de pagamento
│       │   ├── consumers/    # lê as respostas do pagamento
│       │   ├── entities/     # tabelas
│       │   └── repositories/
│       └── resources/
│           ├── static/       # CSS, JS e imagens
│           └── templates/    # telas Thymeleaf
├── marketbuddies-worker/     # integração de pagamentos (sem porta)
│   └── src/main/java/.../worker/
│       ├── consumers/        # lê o pedido de pagamento
│       ├── clients/          # chama o Tranquilo Bank
│       └── producers/        # publica a resposta
├── tranquilo-bank/           # API do banco (porta 8090)
├── postman/                  # coleção e exemplos de JSON
├── assets/                   # imagens deste README
└── docker-compose.yml        # PostgreSQL + RabbitMQ local
```

---

## Como Executar

### Pré-requisitos

- Java 21+
- Docker (para PostgreSQL e RabbitMQ)

### Rodando localmente

```bash
# Clonar o repositório
git clone https://github.com/3BugBuddies/cp5-market-buddies.git
cd cp5-market-buddies

# Subir PostgreSQL e RabbitMQ
docker compose up -d
```

Depois, rodar pela IDE (ou com `./mvnw spring-boot:run` dentro de cada pasta) as três aplicações **nesta ordem**:

1. `TranquiloBankApplication` — porta 8090
2. `MarketbuddiesWorkerApplication` — sem porta
3. `MarketbuddiesApiApplication` — porta 8080

A ordem não é rígida: api e worker declaram as mesmas exchanges e filas no RabbitMQ (a declaração é idempotente), e o worker tenta o banco 3 vezes antes de recusar com "Banco indisponível". Subir o banco primeiro evita a recusa por indisponibilidade na primeira compra.

| Recurso local | Endereço |
|---------------|----------|
| MarketBuddies API | http://localhost:8080 |
| Tranquilo Bank | http://localhost:8090 |
| MarketBuddies Worker | sem porta (só fila e HTTP de saída) |
| Painel do RabbitMQ | http://localhost:15672 (guest / guest, só local) |
| PostgreSQL | `localhost:5432`, banco `marketbuddies` |

Para o envio de e-mail local, exporte `MAIL_USERNAME` e `MAIL_PASSWORD` (senha de app do Gmail). Sem eles, o pagamento continua funcionando e o envio é registrado como `ERROR`.

---

## Segurança — Spring Security + JWT

![Fluxo de segurança com JWT](assets/seguranca-jwt.png)

Seguimos o padrão JWT da aula de Spring Security, sem sessão, nas telas e na API. Depois do login, o token vai num cookie protegido nas telas e no header `Authorization: Bearer` na API (Postman). Toda requisição passa pelo mesmo filtro, que valida o token e identifica o usuário.

- **Perfis:** o CUSTOMER compra e vê os próprios pedidos; o ADMIN gerencia os produtos e o dashboard e não compra. O visitante navega pela vitrine e pelo carrinho, e só faz login para finalizar a compra.
- **Sem permissão:** as telas mostram a página 403 e a API responde 401 ou 403 em JSON.
- **Autenticação automática:** não declaramos o provider de autenticação, como na aula. Com um único serviço de usuários e um único codificador de senha (BCrypt), o Spring Security monta o provider sozinho.
- **Cadastro:** cria sempre um CUSTOMER, com e-mail único e senha guardada como hash.
- **CSRF:** fica ligado nas telas, porque o cookie vai sozinho em toda requisição, e desligado na API, que usa o header.

<!-- print pendente: assets/tela-login.png (ver Demonstração Visual) -->

---

## Tratamento de Erros

| Camada | Tratamento |
|--------|-----------|
| Telas | `ViewExceptionHandler` (`@ControllerAdvice`) renderiza `error/404` e `error/403`; erro de validação reabre o formulário (ou o modal) com a mensagem no campo |
| API (`/api/**`) | `ApiExceptionHandler` (`@RestControllerAdvice`) devolve JSON `{timestamp, status, error, message, path, errors?}` |
| Tranquilo Bank | mesmo corpo JSON; `400` para corpo inválido, com os erros por campo |

| Situação na API | Status |
|-----------------|-------:|
| Corpo inválido (lista vazia, quantidade < 1) | 400 |
| Sem token ou token inválido | 401 |
| Perfil sem permissão | 403 |
| Produto ou pedido inexistente | 404 |
| Sem estoque (`OutOfStockException`), nenhum item baixa | 409 |

---

## Mensageria — RabbitMQ

![Fluxo de pagamento](assets/fluxo-pagamento.png)

### Exchanges, routing keys e filas

Três exchanges `direct` duráveis, uma por mensagem, como na demo do professor. A api **e** o worker declaram as três exchanges, as três filas e os três bindings (`RabbitMQConfig`), então qualquer serviço pode subir primeiro.

| Fluxo | Exchange | Routing key | Fila | Publica → consome |
|-------|----------|-------------|------|-------------------|
| Pedido de pagamento | `payment-request-exchange` | `payment-request-routing-key` | `payment-request-queue` | api (`PaymentRequestProducer`) → worker (`PaymentRequestConsumer`) |
| Resposta aprovada | `payment-response-success-exchange` | `payment-response-success-routing-key` | `payment-response-success-queue` | worker (`PaymentSuccessProducer`) → api (`PaymentResponseSuccessConsumer`) |
| Resposta recusada | `payment-response-error-exchange` | `payment-response-error-routing-key` | `payment-response-error-queue` | worker (`PaymentErrorProducer`) → api (`PaymentResponseErrorConsumer`) |

As mensagens são JSON (`JacksonJsonMessageConverter`, Jackson 3). Mensagem que falha no consumidor é descartada e logada (`default-requeue-rejected: false`), em vez de voltar à fila em loop.

### Correspondência com a demo do professor

Os nomes seguem a demo `RabbitmqSpringBoot` (`demo-backend-api` + `demo-backend-worker`), traduzidos para inglês:

| Demo do professor | MarketBuddies |
|-------------------|---------------|
| `pagamento-request-exchange` / `-rout-key` / `-queue` | `payment-request-exchange` / `-routing-key` / `-queue` |
| `pagamento-response-sucesso-*` | `payment-response-success-*` |
| `pagamento-response-erro-*` | `payment-response-error-*` |
| `PagamentoFacade` | `PaymentFacade` |
| `PagamentoRequestProducer` | `PaymentRequestProducer` |
| `PagamentoResponseSucessoConsumidor` / `PagamentoResponseErroConsumidor` | `PaymentResponseSuccessConsumer` / `PaymentResponseErrorConsumer` |
| `PagamentoRequestConsumidor` (worker) | `PaymentRequestConsumer` |
| `PagamentoSucessoProdutor` / `PagamentoErroProdutor` | `PaymentSuccessProducer` / `PaymentErrorProducer` |
| `PagamentoDTO` | `PaymentRequestDto` / `PaymentResponseDto` |

Na demo, as exchanges e filas eram criadas no painel; aqui são declaradas em código, como o `RabbitmqSpringEmail` faz com a fila de e-mail.

### Mensagens

`PaymentRequestDto` (api → worker), pedido 42 com 1 café e 2 bananas:

```json
{
  "orderId": 42,
  "customerEmail": "cliente@marketbuddies.com",
  "customerName": "Ana Souza",
  "totalAmount": 30.47,
  "items": [
    { "productId": 7, "productName": "Café torrado e moído 500 g", "quantity": 1, "unitPrice": 18.71 },
    { "productId": 1, "productName": "Banana prata (kg)", "quantity": 2, "unitPrice": 5.88 }
  ],
  "requestedAt": "2026-10-18T20:52:00"
}
```

`PaymentResponseDto` (worker → api), aprovado:

```json
{ "orderId": 42, "approved": true, "authorizationCode": "TB-7F3A9C21", "reason": null, "respondedAt": "2026-10-18T20:52:01" }
```

Recusado:

```json
{ "orderId": 43, "approved": false, "authorizationCode": null, "reason": "Valor acima do limite de R$ 500,00", "respondedAt": "2026-10-18T20:53:01" }
```

### Regra do Tranquilo Bank

O banco **recusa** quando o total é **maior que R$ 500,00** (`tranquilo-bank.approval-limit`, variável `APPROVAL_LIMIT`); caso contrário aprova e devolve um código `TB-` + 8 caracteres hexadecimais. A decisão de negócio volta sempre com `200`, aprovada ou recusada. O banco não guarda transações: cada decisão vai para o log (`Pedido 42 · R$ 33,83 · APROVADO TB-7F3A9C21`), e o registro oficial fica no pedido da api.

### Banco indisponível

O `TranquiloBankClient` tenta **3 vezes**, com 1 segundo de espera entre elas. Se o banco continuar fora do ar, o worker publica uma resposta de erro com o motivo "Banco indisponível". A api marca o pedido como REFUSED e devolve o estoque reservado.

### Estado do pedido e idempotência

O pedido nasce `AWAITING_PAYMENT`, com o estoque já reservado de forma atômica, e só é publicado na fila **depois do commit**. A resposta muda o status por um `update` condicional (`… where status = 'AWAITING_PAYMENT'`): resposta repetida ou concorrente recebe 0 linhas e é ignorada, sem segundo e-mail nem segunda devolução de estoque. Um job marca como REFUSED ("Tempo esgotado") o pedido que fica mais de 5 minutos sem resposta.

<!-- print pendente: assets/worker-log.png (log do worker/banco com "APROVADO TB-…") -->

---

## E-mail de Confirmação

Enviado pelo **Gmail** (SMTP `smtp.gmail.com:587`, STARTTLS, senha de app) **somente quando o pagamento é aprovado**. Pedido recusado não gera e-mail.

Como no `EmailService` do professor (`RabbitmqSpringEmail`), cada tentativa é gravada em `TB_EMAILS` (`EmailEntity`: `emailFrom`, `emailTo`, `subject`, `text`, `sendDateEmail`, `statusEmail`):

- envio ok: `SENT`;
- falha do SMTP (`MailException`): `ERROR` e a mensagem em `error_message`; o erro nunca volta para a fila nem desfaz o pagamento aprovado.

O envio acontece depois que o status do pedido foi gravado, para não segurar conexão do banco durante o SMTP. Se o e-mail falhar, o dashboard mostra o badge de falha e o ADMIN pode usar "Reenviar e-mail".

**Assunto:** `MarketBuddies · Pagamento confirmado do pedido #42`. **Corpo:** nome do cliente, itens com quantidade e preço, total, código de autorização e "Obrigado por comprar no MarketBuddies!".

<!-- print pendente: assets/email-confirmacao.png (e-mail recebido no Gmail) -->
<!-- print pendente: assets/banco-emails.png (select em TB_EMAILS com SENT/ERROR) -->

---

## Variáveis de Ambiente

Nenhuma senha fica no repositório. Cada `application.yaml` lê as configurações no formato `${VARIAVEL:padrão}`: o padrão é o ambiente local do `docker-compose.yml`, e no Railway as variáveis são cadastradas em cada serviço.

### MarketBuddies API

| Variável | Padrão local | Descrição |
|----------|--------------|-----------|
| `PORT` | `8080` | Porta HTTP (o Railway injeta a dele) |
| `DB_URL` | `jdbc:postgresql://localhost:5432/marketbuddies` | URL JDBC do PostgreSQL |
| `DB_USER` | `marketbuddies` | Usuário do banco |
| `DB_PASSWORD` | `marketbuddies` | Senha do banco |
| `DDL_AUTO` | `update` | Estratégia do Hibernate para o schema |
| `SHOW_SQL` | `false` | Mostra o SQL no log |
| `RABBITMQ_HOST` | `localhost` | Host do RabbitMQ |
| `RABBITMQ_PORT` | `5672` | Porta AMQP |
| `RABBITMQ_USER` | `guest` | Usuário do RabbitMQ |
| `RABBITMQ_PASSWORD` | `guest` | Senha do RabbitMQ |
| `MAIL_HOST` | `smtp.gmail.com` | Servidor SMTP |
| `MAIL_PORT` | `587` | Porta SMTP (STARTTLS) |
| `MAIL_USERNAME` | — | Conta Gmail que envia as confirmações (também é o remetente) |
| `MAIL_PASSWORD` | — | Senha de app do Gmail (exige verificação em duas etapas) |
| `JWT_SECRET` | chave de desenvolvimento | Chave HS256 do token, 32 caracteres ou mais. No Railway é uma chave aleatória |
| `ADMIN_EMAIL` | `admin@marketbuddies.com` | E-mail do ADMIN criado pelo seed |
| `ADMIN_PASSWORD` | `admin123` | Senha do ADMIN. O padrão só vale local; no Railway é obrigatório trocar |

### MarketBuddies Worker

| Variável | Padrão local | Descrição |
|----------|--------------|-----------|
| `RABBITMQ_HOST` | `localhost` | Host do RabbitMQ |
| `RABBITMQ_PORT` | `5672` | Porta AMQP |
| `RABBITMQ_USER` | `guest` | Usuário do RabbitMQ |
| `RABBITMQ_PASSWORD` | `guest` | Senha do RabbitMQ |
| `TRANQUILO_BANK_URL` | `http://localhost:8090` | Endereço da API do Tranquilo Bank (no Railway, a URL da rede privada) |

### Tranquilo Bank

| Variável | Padrão local | Descrição |
|----------|--------------|-----------|
| `PORT` | `8090` | Porta HTTP |
| `APPROVAL_LIMIT` | `500.00` | Valor acima do qual o pagamento é recusado |

---

## Credenciais de Teste

| Perfil | E-mail | Senha |
|--------|--------|-------|
| CUSTOMER | `cliente@marketbuddies.com` | `cliente123` |
| ADMIN | informado no `.txt` da entrega | informado no `.txt` da entrega |

A conta de cliente é criada pelo seed (nome "Ana Souza") e aparece num quadro "Conta de teste" na tela de login. A senha do ADMIN fica só no `.txt` da entrega, porque a conta dá acesso à gestão de produtos e ao dashboard.

---

## Deploy no Railway

Projeto `marketbuddies` com **5 serviços**:

| Serviço | Origem | Root directory | Domínio |
|---------|--------|----------------|---------|
| `marketbuddies-api` | este repositório | `/marketbuddies-api` | público |
| `marketbuddies-worker` | este repositório | `/marketbuddies-worker` | nenhum |
| `tranquilo-bank` | este repositório | `/tranquilo-bank` | público (para o Postman) |
| `Postgres` | PostgreSQL 18 | — | proxy TCP (conexão externa) |
| `rabbitmq` | imagem `rabbitmq:4-management` | — | público só o painel (15672) |

**Build.** Cada serviço do repositório tem o seu `Dockerfile` (multi-stage, Maven + JRE 21) e usa a sua pasta como diretório raiz. Como o campo de builder do Railway não aceitou `DOCKERFILE` pelo CLI, cada serviço define `RAILWAY_DOCKERFILE_PATH=Dockerfile`. Os watch paths por pasta disparam o deploy só do serviço cuja pasta mudou.

**Variáveis por referência.** O banco e o RabbitMQ não têm valor copiado: a api lê as variáveis dos outros serviços.

```
DB_URL              = jdbc:postgresql://${{Postgres.PGHOST}}:${{Postgres.PGPORT}}/${{Postgres.PGDATABASE}}
DB_USER             = ${{Postgres.PGUSER}}
DB_PASSWORD         = ${{Postgres.PGPASSWORD}}
RABBITMQ_HOST       = rabbitmq.railway.internal
TRANQUILO_BANK_URL  = http://${{tranquilo-bank.RAILWAY_PRIVATE_DOMAIN}}:8090     # só no worker
```

A `DB_URL` é montada com as variáveis `PG*` e começa com `jdbc:` (a variável pronta do Railway começa com `postgresql://`, que o driver JDBC não aceita). Os serviços falam entre si pela **rede privada** do Railway; o worker não precisa de domínio público.

**Memória das JVMs.** O plano de teste tem crédito limitado, então cada JVM tem teto de memória (`JAVA_TOOL_OPTIONS`):

| Serviço | Opções |
|---------|--------|
| api | `-Xmx256m -XX:MaxMetaspaceSize=192m -XX:+UseSerialGC` |
| worker e tranquilo-bank | `-Xmx128m -XX:+UseSerialGC` |

Os Dockerfiles sobem com `-Duser.timezone=America/Sao_Paulo`, para o "hoje" do dashboard e os horários dos pedidos seguirem o horário de Brasília. Atrás do proxy do Railway, `server.forward-headers-strategy: framework` faz os redirects saírem com `https` e o domínio público.

**Consumo.** Medição preliminar com os serviços ociosos: cerca de 0,8 GB somados, o que faz o crédito inicial durar aproximadamente 17 dias. A medição final, com o sistema rodando, entra no aviso do topo deste README (_a preencher_). Se o crédito acabar antes, o `tranquilo-bank` é o primeiro a cair: o worker passa a responder "Banco indisponível" e o PostgreSQL é o último a sair.

<!-- print pendente: assets/railway-servicos.png (projeto com os 5 serviços no ar) -->

### Rodando com Docker

Cada serviço tem `Dockerfile` próprio. Exemplo para o banco:

```bash
docker build -t tranquilo-bank ./tranquilo-bank
docker run -p 8090:8090 tranquilo-bank
```

---

## Modelo de Dados

![Diagrama ER](assets/diagrama-er.png)

<!-- print pendente: assets/banco-tabelas.png (as 5 tabelas no PostgreSQL) -->

---

## Fluxo de Telas

| Rota | Acesso | Descrição |
|------|--------|-----------|
| `GET /` | público | Landing page |
| `GET /login` | público | Login próprio (`?error`, `?logout`, `?registered`), com quadro "Conta de teste" |
| `POST /login` | público | Autentica, grava o cookie `MB_TOKEN` e redireciona por perfil |
| `GET/POST /signup` | público | Cadastro de CUSTOMER; sucesso volta ao login com "Conta criada!" |
| `POST /logout` | logado | Apaga o cookie e volta ao login |
| `GET /products` | público | Vitrine com busca e filtro por categoria |
| `GET /products/{id}` | público | Detalhe do produto, com oferta e estoque |
| `GET /cart` | público | Carrinho (na sessão HTTP); visitante vê "Entrar para finalizar" |
| `POST /cart/add`, `/cart/update`, `/cart/remove` | público | Altera o carrinho, limitado ao estoque |
| `GET /checkout` | CUSTOMER | Revisar e pagar (recalcula com o preço atual) |
| `POST /checkout` | CUSTOMER | Cria o pedido, esvazia o carrinho e vai para `/orders/{id}` |
| `GET /orders/{id}` | CUSTOMER dono | Pedido: aguardando, aprovado ou recusado |
| `GET /orders/{id}/status` | CUSTOMER dono | JSON `{status}` consultado a cada 2 s pela tela de aguardando |
| `POST /orders/{id}/retry` | CUSTOMER dono | "Tentar de novo": remonta o carrinho com os itens do pedido recusado |
| `GET /admin/dashboard` | ADMIN | KPIs, gráficos e últimas compras |
| `POST /admin/orders/{id}/resend-email` | ADMIN | Reenvia o e-mail de um pedido aprovado cujo envio falhou |
| `GET /admin/products` | ADMIN | Gestão: tabela de produtos e modal de edição |
| `POST /admin/products` | ADMIN | Cria produto |
| `POST /admin/products/{id}` | ADMIN | Atualiza produto |
| `POST /admin/products/{id}/delete` | ADMIN | Exclui produto (com confirmação em modal) |
| `GET /403` | logado | Página 403 estilizada |

Erro de validação no modal de gestão reabre o modal com a mensagem no campo. O preço nunca é digitado: o modal só mostra uma prévia calculada.

---

## Demonstração Visual

Prints na ordem do fluxo no navegador, com ênfase no **desenvolvimento e na tela de login**.

1. **Landing page (`/`)** — entrada pública, com "Entrar" e "Criar conta" na navbar.
   <!-- print pendente: assets/tela-landing.png -->
2. **Login** — tela própria do MarketBuddies (a tela padrão do Spring nunca aparece), com o quadro "Conta de teste".
   <!-- print pendente: assets/tela-login.png -->
3. **Login com credenciais inválidas (`/login?error`)** — alerta de erro na própria tela.
   <!-- print pendente: assets/tela-login-erro.png -->
4. **Cadastro com erro de validação** — mensagens por campo, sem sair da tela.
   <!-- print pendente: assets/tela-cadastro.png -->
5. **Conta criada (`/login?registered`)** — volta ao login com o aviso de sucesso.
   <!-- print pendente: assets/tela-cadastro-sucesso.png -->
6. **Vitrine (`/products`)** — chips de categoria e cards com preço, oferta e estoque.
   <!-- print pendente: assets/tela-vitrine.png -->
7. **Detalhe do produto** — Café em oferta, com o preço antigo riscado.
   <!-- print pendente: assets/tela-detalhe.png -->
8. **Gestão de produtos (ADMIN)** — tabela com editar e excluir por linha.
   <!-- print pendente: assets/tela-gestao.png -->
9. **Modal de edição** — formulário sem campo de preço, com a prévia do preço calculado.
   <!-- print pendente: assets/tela-modal-editar.png -->
10. **Modal com erro de validação** — o modal reabre com a mensagem no campo.
    <!-- print pendente: assets/tela-modal-erro.png -->
11. **Confirmação de exclusão** — modal pequeno antes de excluir.
    <!-- print pendente: assets/tela-modal-excluir.png -->
12. **Página 403** — cliente tentando abrir `/admin/products`.
    <!-- print pendente: assets/tela-403.png -->
13. **Carrinho do visitante** — resumo e o botão "Entrar para finalizar".
    <!-- print pendente: assets/tela-carrinho.png -->
14. **Checkout** — revisar os itens e pagar.
    <!-- print pendente: assets/tela-checkout.png -->
15. **Pedido aguardando pagamento** — a tela consulta o status a cada 2 s.
    <!-- print pendente: assets/tela-pedido-aguardando.png -->
16. **Pagamento confirmado** — pedido aprovado, com o código de autorização.
    <!-- print pendente: assets/tela-pedido-aprovado.png -->
17. **Pagamento recusado** — motivo da recusa e o botão "Tentar de novo".
    <!-- print pendente: assets/tela-pedido-recusado.png -->
18. **Dashboard da gerência** — KPIs, gráficos e últimas compras (detalhado na seção própria).
    <!-- print pendente: assets/tela-dashboard.png -->
19. **Vitrine no celular (375 px)** — layout sem rolagem horizontal.
    <!-- print pendente: assets/tela-celular.png -->

---

## Testes com Postman

A coleção está em [`postman/marketbuddies.postman_collection.json`](postman/marketbuddies.postman_collection.json); os JSONs completos estão em [`postman/exemplos.md`](postman/exemplos.md).

### Como importar

1. Postman → **Import** → selecione `postman/marketbuddies.postman_collection.json`.
2. Confira as variáveis da coleção:

| Variável | Valor padrão | Uso |
|----------|--------------|-----|
| `{{api}}` | `https://marketbuddies-api-production.up.railway.app` | URL da API (local: `http://localhost:8080`) |
| `{{bank}}` | `https://tranquilo-bank-production.up.railway.app` | URL do Tranquilo Bank (local: `http://localhost:8090`) |
| `{{clientEmail}}` / `{{clientPassword}}` | `cliente@marketbuddies.com` / `cliente123` | Login do cliente de teste |
| `{{adminEmail}}` / `{{adminPassword}}` | vazias | Preencher com o ADMIN do `.txt` da entrega |
| `{{token}}` / `{{adminToken}}` / `{{orderId}}` | vazias | Preenchidas pelos scripts de teste |

3. Rode **1. Autenticação → Login (cliente)** primeiro.

### Fluxo do login

A request de login chama `POST {{api}}/api/auth/login`, e o script de teste dela grava o `accessToken` da resposta em `{{token}}` (o login do ADMIN grava em `{{adminToken}}`). As demais requests usam `Authorization: Bearer {{token}}`. A request de compra grava o `id` do pedido em `{{orderId}}`, que a consulta do pedido reaproveita. Cada request tem testes de status.

### Requests

**1. Login (cliente)** — `POST {{api}}/api/auth/login`, resposta `200`.

```json
{ "email": "cliente@marketbuddies.com", "password": "cliente123" }
```

```json
{ "accessToken": "eyJhbGciOiJIUzI1NiJ9...", "tokenType": "Bearer", "expiresIn": 3600000 }
```

<!-- print pendente: assets/postman-login.png -->

**2. Login inválido** — mesma request com senha errada, resposta `401`.

```json
{ "timestamp": "2026-10-18T20:50:00", "status": 401, "error": "Unauthorized", "message": "Usuário ou senha inválidos", "path": "/api/auth/login" }
```

<!-- print pendente: assets/postman-login-invalido.png -->

**3. Listar produtos** — `GET {{api}}/api/products`, público, resposta `200` com a lista de produtos (com preço, preço final e desconto; sem custo nem markup).

<!-- print pendente: assets/postman-produtos.png -->

**4. Compra aprovada** — `POST {{api}}/api/orders`, `Bearer`, total de R$ 30,47 (abaixo do limite), resposta `202 Accepted` com `Location: /api/orders/42`. O comprador é o dono do token.

```json
{ "items": [ { "productId": 7, "quantity": 1 }, { "productId": 1, "quantity": 2 } ] }
```

```json
{
  "id": 42,
  "status": "AWAITING_PAYMENT",
  "totalAmount": 30.47,
  "customerEmail": "cliente@marketbuddies.com",
  "createdAt": "2026-10-18T20:52:00",
  "items": [
    { "productName": "Café torrado e moído 500 g", "quantity": 1, "unitPrice": 18.71 },
    { "productName": "Banana prata (kg)", "quantity": 2, "unitPrice": 5.88 }
  ]
}
```

<!-- print pendente: assets/postman-compra-aprovada.png -->

**5. Consultar pedido aprovado** — `GET {{api}}/api/orders/{{orderId}}` cerca de 2 s depois, resposta `200`.

```json
{
  "id": 42,
  "status": "APPROVED",
  "totalAmount": 30.47,
  "customerEmail": "cliente@marketbuddies.com",
  "createdAt": "2026-10-18T20:52:00",
  "respondedAt": "2026-10-18T20:52:01",
  "authorizationCode": "TB-7F3A9C21",
  "refusalReason": null,
  "statusEmail": "SENT",
  "items": [
    { "productName": "Café torrado e moído 500 g", "quantity": 1, "unitPrice": 18.71 },
    { "productName": "Banana prata (kg)", "quantity": 2, "unitPrice": 5.88 }
  ]
}
```

<!-- print pendente: assets/postman-pedido-aprovado.png -->

**6. Compra recusada (acima de R$ 500)** — `POST {{api}}/api/orders`, 17 cafés + 21 maçãs = R$ 524,08. Responde `202` (o pedido nasce aguardando) e, na consulta seguinte, `REFUSED`.

```json
{ "items": [ { "productId": 7, "quantity": 17 }, { "productId": 2, "quantity": 21 } ] }
```

```json
{
  "id": 43,
  "status": "REFUSED",
  "totalAmount": 524.08,
  "customerEmail": "cliente@marketbuddies.com",
  "createdAt": "2026-10-18T20:53:00",
  "respondedAt": "2026-10-18T20:53:01",
  "authorizationCode": null,
  "refusalReason": "Valor acima do limite de R$ 500,00",
  "statusEmail": null
}
```

<!-- print pendente: assets/postman-compra-recusada.png -->

**7. Compra sem estoque** — `POST {{api}}/api/orders` com `quantity` 999, resposta `409`; nenhum item baixa.

```json
{ "timestamp": "2026-10-18T20:54:00", "status": 409, "error": "Conflict", "message": "Sem estoque para Café torrado e moído 500 g (pedido 999, disponível 18)", "path": "/api/orders" }
```

<!-- print pendente: assets/postman-sem-estoque.png -->

**8. Compra sem token** — `POST {{api}}/api/orders` sem o header `Authorization`, resposta `401`.

```json
{ "timestamp": "2026-10-18T20:54:30", "status": 401, "error": "Unauthorized", "message": "Autenticação necessária", "path": "/api/orders" }
```

<!-- print pendente: assets/postman-sem-token.png -->

**9. Corpo inválido** — `POST {{api}}/api/orders` com `{ "items": [] }`, resposta `400`.

```json
{
  "timestamp": "2026-10-18T20:55:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Dados inválidos",
  "path": "/api/orders",
  "errors": { "items": "O pedido precisa ter ao menos um item" }
}
```

**10. E-mails enviados** — `GET {{api}}/api/emails` com o `Bearer` de ADMIN, resposta `200` com os registros de `TB_EMAILS` (`SENT`/`ERROR`), do mais recente ao mais antigo.

<!-- print pendente: assets/postman-emails.png -->

**11. Tranquilo Bank — aprovado** — `POST {{bank}}/api/payments/authorize`, resposta `200`.

```json
{ "orderId": 42, "amount": 33.83, "customerEmail": "cliente@marketbuddies.com" }
```

```json
{ "orderId": 42, "approved": true, "authorizationCode": "TB-7F3A9C21", "reason": null, "processedAt": "2026-10-18T20:52:01" }
```

<!-- print pendente: assets/postman-banco-aprovado.png -->

**12. Tranquilo Bank — recusado** — mesma request com `"amount": 523.88`, resposta `200`.

```json
{ "orderId": 43, "approved": false, "authorizationCode": null, "reason": "Valor acima do limite de R$ 500,00", "processedAt": "2026-10-18T20:53:01" }
```

<!-- print pendente: assets/postman-banco-recusado.png -->

**13. Tranquilo Bank — inválido** — mesma request sem `amount`, resposta `400`.

```json
{
  "timestamp": "2026-10-18T20:56:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Dados inválidos",
  "path": "/api/payments/authorize",
  "errors": { "amount": "O valor é obrigatório" }
}
```

<!-- print pendente: assets/postman-banco-invalido.png -->

**Collection Runner** — todos os testes da coleção passando.

<!-- print pendente: assets/postman-runner.png -->

> Os JSONs acima são os de `postman/exemplos.md`, escritos a partir dos contratos da spec. Serão trocados pelas respostas reais quando os prints forem tirados.

---

## Painel do RabbitMQ e Testes Internos

Painel no Railway: https://rabbitmq-production-e3dc.up.railway.app (usuário e senha no `.txt` da entrega). Local: http://localhost:15672.

- **Exchanges** — as três `direct`.
  <!-- print pendente: assets/rabbitmq-exchanges.png -->
- **Filas** — as três filas com mensagens trafegadas depois de uma compra.
  <!-- print pendente: assets/rabbitmq-filas.png -->
- **Mensagem** — "Get messages" mostrando o JSON de um pedido.
  <!-- print pendente: assets/rabbitmq-mensagem.png -->

### Testes internos (RabbitMQ Monitor)

O enunciado pede "testes internos do RabbitMQ Monitor". Roteiro:

1. **Publicar pelo painel:** em `payment-request-exchange`, "Publish message" com um `PaymentRequestDto` e routing key `payment-request-routing-key`. Esperado: uma mensagem chega em `payment-response-success-queue` (valor até R$ 500,00) ou em `payment-response-error-queue` (valor acima).
   <!-- print pendente: assets/rabbitmq-publicar.png (I4) -->
2. **Banco parado:** com o `tranquilo-bank` desligado, publicar de novo. Esperado: depois de 3 tentativas, uma mensagem em `payment-response-error-queue` com "Banco indisponível".
3. **Resposta repetida:** reenviar na fila de sucesso a resposta de um pedido já aprovado. Esperado: nada muda na tela e nenhuma linha nova no banco (log "resposta ignorada").
4. **Compra de ponta a ponta:** comprar pela tela e acompanhar os contadores das três filas.

---

## Dashboard da Gerência

Tela `/admin/dashboard` (ADMIN), com Chart.js. Os dados vêm do banco; o lucro usa o custo gravado no item (foto do momento).

**KPIs:** vendas de hoje; pedidos pagos hoje (com recusados e aguardando); ticket médio; lucro de hoje e % das vendas; markup médio do catálogo; produtos com estoque ≤ 5.

**Gráficos:** vendas × lucro dos últimos 7 dias; vendas da semana por categoria; estoque por produto (vermelho quando ≤ 5).

**Últimas 10 compras:** tabela com badge de pagamento e de e-mail e o botão "Reenviar e-mail" para pedido aprovado sem e-mail ou com `ERROR`.

<!-- print pendente: assets/tela-dashboard.png -->

---

## Tecnologias Utilizadas

- **Java 21**
- **Spring Boot 4.1.1**
- **Spring Web (MVC) + Thymeleaf**
- **Spring Security + JWT (jjwt)**
- **Spring Data JPA + Hibernate**
- **PostgreSQL**
- **Spring for RabbitMQ** (RabbitMQ 4)
- **Java Mail Sender** (Gmail SMTP)
- **Chart.js** (dashboard)
- **Lombok**
- **Postman**
- **Docker** e **Railway**
- **Maven**
