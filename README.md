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

Três serviços Spring Boot no mesmo repositório, um para cada caixa do diagrama do enunciado:

| Serviço | Pasta | Caixa do diagrama do enunciado | Papel |
|---------|-------|--------------------------------|-------|
| MarketBuddies API | `marketbuddies-api/` | Back End API + Front end Web | Telas Thymeleaf, cadastro e login com JWT, CRUD de produtos, carrinho, checkout, publica o pedido de pagamento, consome as respostas, envia o e-mail de confirmação e registra o envio, dashboard da gerência e API REST para o Postman |
| MarketBuddies Worker | `marketbuddies-worker/` | Integração Pagamentos | Consome o pedido da fila, chama o Tranquilo Bank por HTTP e publica a resposta na fila de sucesso ou de erro |
| Tranquilo Bank | `tranquilo-bank/` | API Plataforma de Pagamentos / Banco Tranquilo | API REST que aprova ou recusa o pagamento por uma regra fixa; não usa RabbitMQ nem banco de dados |

**Por que são três serviços:** o enunciado fala em "conexão para uma API do Banco Tranquilo", em pagamento "confirmado pela API do Banco Tranquilo" e em "executar a API de Integração de Pagamentos". O diagrama da página 4 desenha o Back End API, a caixa assíncrona com a Integração Pagamentos e, fora dela, a API Plataforma de Pagamentos que leva ao Banco Tranquilo. Adotamos a leitura literal do desenho: cada caixa virou um serviço, e a integração fica visível como um processo separado.

A separação `api` + `worker` também segue a demo de mensageria da disciplina (`demo-backend-api` + `demo-backend-worker`).

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

Pacotes no plural, por camada. Cada serviço espelha o projeto do professor de mesmo papel: a **api** segue o `RabbitmqSpringEmail` (`configs`, `controllers`, `consumers`, `producers`, `dtos`, `enums`, `entities`, `repositories`, `services`) e o **worker** segue o `demo-backend-worker` (`consumers`, `producers`, `dtos`, `clients`, `configs`).

```
cp5-market-buddies/
├── marketbuddies-api/                     # loja web + back end (porta 8080)
│   └── src/main/
│       ├── java/br/com/fiap/marketbuddies/api/
│       │   ├── MarketbuddiesApiApplication.java
│       │   ├── configs/                   # DataSeeder (ADMIN, cliente de teste, 8 produtos), UserConfig (PasswordEncoder), AdminProperties;
│       │   │                              #   SecurityConfig e RabbitMQConfig entram nas tarefas de segurança e de mensageria
│       │   ├── controllers/               # XxxViewController (telas) e XxxApiController (/api/**)
│       │   │   └── handlers/              # ViewExceptionHandler (páginas de erro) e ApiExceptionHandler (JSON)
│       │   ├── entities/                  # UserEntity, ProductEntity, OrderEntity, OrderItemEntity, EmailEntity
│       │   ├── enums/                     # Role, Category, OrderStatus, OrderOrigin, StatusEmail
│       │   ├── exceptions/                # BusinessException, NotFoundException, OutOfStockException
│       │   ├── repositories/              # reserva atômica de estoque, devolução e update condicional de status
│       │   ├── security/                  # (a chegar) JwtUtil e JwtAuthFilter
│       │   ├── services/                  # (a chegar) UserDetailsServiceImpl, OrderService, EmailService, DashboardService
│       │   ├── facades/                   # (a chegar) PaymentFacade
│       │   ├── producers/ consumers/      # (a chegar) pedido de pagamento e respostas
│       │   └── dtos/                      # (a chegar)
│       └── resources/
│           ├── application.yaml           # lê variáveis de ambiente, com padrão local
│           ├── static/{css,js,img}/       # CSS do design system, JS do modal e do polling, logos
│           └── templates/                 # fragments/layout.html, error/*; demais telas a chegar
├── marketbuddies-worker/                  # integração de pagamentos (sem porta)
│   └── src/main/java/br/com/fiap/marketbuddies/worker/
│       ├── configs/                       # RabbitMQConfig (3 exchanges, 3 filas, 3 bindings), RestClientConfig
│       ├── consumers/                     # PaymentRequestConsumer
│       ├── producers/                     # PaymentSuccessProducer, PaymentErrorProducer
│       ├── clients/                       # TranquiloBankClient (RestClient, 3 tentativas)
│       └── dtos/                          # PaymentRequestDto, PaymentResponseDto, PaymentItemDto, Authorization*Dto
├── tranquilo-bank/                        # API do banco (porta 8090)
│   └── src/main/java/br/com/fiap/tranquilobank/
│       ├── controllers/                   # PaymentController (POST /api/payments/authorize), ApiExceptionHandler
│       ├── services/                      # AuthorizationService (regra do limite)
│       └── dtos/                          # AuthorizationRequestDto, AuthorizationResponseDto
├── docker-compose.yml                     # PostgreSQL + RabbitMQ para rodar local
├── postman/                               # coleção e JSONs de exemplo
├── assets/                                # imagens deste README
├── design/                                # design system e logos do MarketBuddies
└── integrantes.txt
```

---

## Por que X e não Y

### JWT em tudo, e não sessão

A Aula 13 do professor usa JWT com `SessionCreationPolicy.STATELESS`, `JwtAuthFilter` antes do `UsernamePasswordAuthenticationFilter` e `AuthController` com `AuthenticationManager`. Seguimos o mesmo padrão nas telas **e** na API: um só mecanismo de autenticação em vez de `formLogin` com sessão para as telas e token para a API. O que muda em relação à aula é o transporte: nas telas o token viaja num cookie `HttpOnly` (`MB_TOKEN`); na API, no header `Authorization: Bearer`. O mesmo filtro lê os dois.

### Provider automático, sem `DaoAuthenticationProvider`

Na Aula 13, o `SecurityConfig` declara um `DaoAuthenticationProvider` à mão, injeta nele o `UserDetailsService` e o `PasswordEncoder` e o registra com `.authenticationProvider(...)`. Aqui **não declaramos o provider**. Quando o contexto tem exatamente um `UserDetailsService` e um `PasswordEncoder`, o Spring Security monta sozinho um `DaoAuthenticationProvider` com os dois e o usa no `AuthenticationManager`. Nossa aplicação tem exatamente um de cada: `UserDetailsServiceImpl` (lê `TB_USERS` por e-mail) e o `BCryptPasswordEncoder` da `UserConfig`. O resultado é o mesmo, com menos código; o `AuthenticationManager` continua sendo obtido por `authConfig.getAuthenticationManager()`, como na aula. Se um dia houver dois `UserDetailsService`, a montagem automática deixa de valer e o provider passa a ser declarado.

### `PaymentFacade`, como a `PagamentoFacade` da demo

A facade concentra o fluxo do pagamento (criar o pedido, publicar na fila, tratar aprovação, recusa e expiração, reenviar e-mail), e cada service fica com uma responsabilidade. O controller de tela e o da API chamam a mesma facade, então o checkout pela web e pelo Postman passam pelo mesmo caminho. O worker não tem facade, igual à demo: o consumidor chama o `TranquiloBankClient` e o producer.

### Preço calculado e gravado

O produto guarda `cost` (custo) e `markupPercentage` (markup), e a aplicação calcula `price = cost × (1 + markup)` ao salvar (`@PrePersist` / `@PreUpdate`). O formulário não tem campo de preço, então o valor nunca diverge. Gravar o resultado em coluna (em vez de calcular a cada leitura) deixa a vitrine, o dashboard e o print do banco com a mesma leitura simples. Oferta é um percentual à parte (`discountPercentage`); o preço cobrado é `price × (1 − desconto)`.

### Tabelas em inglês com prefixo `TB_`

O enunciado cita `TDS_Sec_MVC_TB_Mercado` e `TDS_Users_Mercado` como **exemplo** de nome. Usamos o padrão `TB_` do `RabbitmqSpringEmail` do professor (`TB_USERS`, `TB_PRODUCTS`, `TB_ORDERS`, `TB_ORDER_ITEMS`, `TB_EMAILS`), com o código todo em inglês. Os textos de tela, as mensagens de erro e o e-mail continuam em português.

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

### Rotas × perfis

| Rota | Visitante | CUSTOMER | ADMIN |
|------|:---------:|:--------:|:-----:|
| `/`, `/login`, `/signup` | sim | sim | sim |
| `/products`, `/products/{id}`, `/cart/**` | sim | sim | sim (carrinho bloqueado com aviso) |
| `/css/**`, `/js/**`, `/img/**`, `/error` | sim | sim | sim |
| `/checkout/**`, `/orders/**` | redireciona para `/login?redirect=…` | sim (só os próprios pedidos) | 403 |
| `/admin/**` (dashboard, gestão de produtos) | redireciona para `/login` | 403 | sim |
| `GET /api/products`, `POST /api/auth/login` | sim | sim | sim |
| `POST /api/orders` | 401 | sim | 403 |
| `GET /api/orders/{id}` | 401 | só o dono | sim |
| `GET /api/emails` | 401 | 403 | sim |

ADMIN não compra: a navbar esconde o carrinho e o checkout devolve 403.

### Fluxo do JWT

1. **Login nas telas:** `POST /login` (e-mail e senha) autentica com o `AuthenticationManager`, gera o token e grava o cookie `MB_TOKEN` (`HttpOnly`, `SameSite=Lax`, `Secure` atrás do https, `Max-Age` igual à expiração). O redirecionamento volta para a página pedida (`redirect`) ou leva ADMIN a `/admin/dashboard` e CUSTOMER a `/products`.
2. **Login na API:** `POST /api/auth/login` com `{email, password}` devolve `{accessToken, tokenType: "Bearer", expiresIn}`. Credencial errada: 401 "Usuário ou senha inválidos".
3. **Cada requisição:** o `JwtAuthFilter` lê o token do header `Authorization: Bearer …` ou do cookie `MB_TOKEN`, valida e preenche o `SecurityContext`. É por isso que o `sec:authorize` do Thymeleaf funciona sem sessão.
4. **Token:** HS256, `sub` = e-mail e claim `role`, expiração de 1 hora (`app.jwt.expiration-ms`); a chave vem de `JWT_SECRET`.
5. **Logout:** `POST /logout` apaga o cookie e volta para `/login?logout`.
6. **Falhas:** tela sem token vai para `/login?redirect=<url>`; `/api/**` sem token devolve 401 em JSON; perfil errado devolve a página 403 (telas) ou JSON 403 (API).

### CSRF

Como o cookie de autenticação vai em toda requisição das telas, o **CSRF fica ligado nas telas** (`CookieCsrfTokenRepository.withHttpOnlyFalse()`; o Thymeleaf insere o `_csrf` nos formulários). Em `/api/**` fica **desligado**, porque a API autentica por `Bearer`, que o navegador não envia sozinho.

### Cadastro

E-mail único ("Já existe uma conta com esse e-mail"), senha com no mínimo 6 caracteres, confirmação igual à senha, hash BCrypt. O cadastro sempre cria um CUSTOMER; o ADMIN vem do seed.

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

As mensagens são JSON (`Jackson2JsonMessageConverter`). Mensagem que falha no consumidor é descartada e logada (`default-requeue-rejected: false`), em vez de voltar à fila em loop.

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

Cinco tabelas, todas novas, criadas pelo Hibernate (`ddl-auto: update`, como na CP4 e no projeto de e-mail do professor). Os dados iniciais vêm de um `CommandLineRunner` (`DataSeeder`) que só insere se a tabela estiver vazia: 1 ADMIN, 1 cliente de teste e 8 produtos.

### `TB_USERS` — `UserEntity`

| Campo | Tipo | Observação |
|-------|------|-----------|
| `id` | Long (PK) | identity |
| `fullName` | String(120) | obrigatório |
| `email` | String(160) | único, usado como login |
| `password` | String(100) | hash BCrypt |
| `role` | enum `Role` | `CUSTOMER` ou `ADMIN` |
| `createdAt` | LocalDateTime | preenchido ao criar |

### `TB_PRODUCTS` — `ProductEntity`

| Campo | Tipo | Observação |
|-------|------|-----------|
| `id` | Long (PK) | identity |
| `name` | String(80) | obrigatório |
| `category` | enum `Category` | `PRODUCE`, `CLEANING`, `BEVERAGES`, `BAKERY`, `GROCERY` (rótulo em português por `getLabel()`) |
| `description` | String(500) | opcional |
| `cost` | BigDecimal(15,2) | custo |
| `markupPercentage` | BigDecimal(7,2) | `40.00` = 40% |
| `price` | BigDecimal(15,2) | **calculado**: `cost × (1 + markup)` |
| `discountPercentage` | BigDecimal(5,2) | `0` = sem oferta |
| `stock` | Integer | estoque |
| `version` | Long (`@Version`) | trava otimista: o admin que edita com estoque desatualizado recebe aviso em vez de apagar uma reserva feita no meio |

O preço cobrado (`finalPrice`) é calculado em tempo de execução, sem coluna.

### `TB_ORDERS` — `OrderEntity`

| Campo | Tipo | Observação |
|-------|------|-----------|
| `id` | Long (PK) | identity |
| `user_id` | FK → `TB_USERS` | o cliente |
| `status` | enum `OrderStatus` | `AWAITING_PAYMENT`, `APPROVED`, `REFUSED` |
| `totalAmount` | BigDecimal(15,2) | total do pedido |
| `createdAt` / `respondedAt` | LocalDateTime | criação e resposta do banco |
| `authorizationCode` | String(40) | código `TB-…` do banco, quando aprovado |
| `refusalReason` | String(200) | motivo da recusa |
| `origin` | enum `OrderOrigin` | `WEB` ou `API` |

### `TB_ORDER_ITEMS` — `OrderItemEntity`

| Campo | Tipo | Observação |
|-------|------|-----------|
| `id` | Long (PK) | identity |
| `order_id` | FK → `TB_ORDERS` | obrigatório |
| `product_id` | FK → `TB_PRODUCTS` | **anulável**: fica nulo se o produto for excluído |
| `productName` | String(80) | cópia do nome |
| `category` | enum `Category` | cópia, para o gráfico por categoria |
| `quantity` | Integer | — |
| `unitPrice` | BigDecimal(15,2) | preço final do dia (com oferta) |
| `unitCost` | BigDecimal(15,2) | custo do dia |

**Foto do momento.** O item do pedido copia nome, categoria, preço e custo da hora da compra. Editar ou excluir o produto depois não pode mudar o que foi vendido, nem o lucro, nem o gráfico por categoria. A cópia é histórica, não uma duplicação do cadastro; por isso `product_id` é anulável e a exclusão de um produto já vendido apenas solta a referência.

### `TB_EMAILS` — `EmailEntity`

| Campo | Tipo | Observação |
|-------|------|-----------|
| `id` | Long (PK) | identity |
| `order_id` | FK → `TB_ORDERS` | pedido que gerou o e-mail |
| `emailFrom` / `emailTo` | String(160) | remetente e destinatário |
| `subject` | String(200) | assunto |
| `text` | TEXT | corpo |
| `sendDateEmail` | LocalDateTime | quando foi tentado |
| `statusEmail` | enum `StatusEmail` | `SENT` ou `ERROR` |
| `errorMessage` (`error_message`) | String(500) | mensagem do SMTP, quando `ERROR` |

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

---

## Roteiro do Vídeo (5–7 min)

| Tempo | O que mostrar |
|-------|---------------|
| 0:00–0:30 | Apresentação do grupo e dos 3 serviços, com o diagrama de arquitetura |
| 0:30–1:30 | **Código:** `SecurityConfig` e `JwtAuthFilter`; `PaymentFacade`; `RabbitMQConfig`; `TranquiloBankClient`; `EmailService` |
| 1:30–2:30 | **Telas:** landing, login (erro e cadastro), vitrine, detalhe e página 403 para o cliente |
| 2:30–3:30 | **Compra aprovada:** carrinho de visitante → login → checkout → pedido aguardando → confirmado |
| 3:30–4:00 | **Compra recusada:** acima de R$ 500,00, motivo na tela e "Tentar de novo" |
| 4:00–4:45 | **Painel do RabbitMQ:** exchanges, filas, mensagens trafegadas e o teste interno de publicação |
| 4:45–5:30 | **Postman:** login que grava o token, compra aprovada, recusada e sem estoque |
| 5:30–6:00 | **E-mail** recebido no Gmail e registro `SENT` em `TB_EMAILS` |
| 6:00–6:45 | **Dashboard** da gerência e gestão de produtos (CRUD pelo modal) |
| 6:45–7:00 | Deploy no Railway e encerramento |
