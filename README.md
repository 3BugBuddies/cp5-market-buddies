# MarketBuddies — CheckPoint 5 | Java Advanced

Mercado express com loja web autenticada (Spring Security + JWT + Thymeleaf + PostgreSQL) e pagamento assíncrono pelo **Tranquilo Bank** via RabbitMQ, com e-mail de confirmação e dashboard da gerência. Entrega única das **Partes I e II** do CheckPoint 5 da disciplina de Java Advanced (TDS) — FIAP.

- **Parte I:** loja web com landing, login e cadastro próprios, perfis CUSTOMER e ADMIN, CRUD de produtos pelo ADMIN e página 403 estilizada, tudo no PostgreSQL.
- **Parte II:** carrinho, checkout, pedido de pagamento publicado no RabbitMQ, integração com o Tranquilo Bank, resposta assíncrona, e-mail de confirmação registrado no banco e dashboard da gerência.

---

## Deploy

| Recurso | URL |
|---------|-----|
| Aplicação — MarketBuddies API (Railway) | https://marketbuddies-api-production.up.railway.app |
| Login | https://marketbuddies-api-production.up.railway.app/login |
| Tranquilo Bank (API de pagamentos) | https://tranquilo-bank-production.up.railway.app |
| Painel do RabbitMQ | https://rabbitmq-production-e3dc.up.railway.app |
| Vídeo | _a preencher_ |

> O deploy roda no plano de teste do Railway, que tem **crédito limitado**. Previsão de ficar no ar até **01/11/2026** (consumo medido: ~886 MB de RAM somados nos 5 serviços, ~US$ 0,32/dia). Depois disso, a aplicação pode ficar indisponível; o vídeo e os prints deste README mostram o fluxo completo.

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

Spring Boot 4.1.1, Java 21, Maven, configuração em YAML. Cada serviço foi gerado separadamente no Initializr (clique na imagem para ver em tamanho real).

<table>
  <tr>
    <th>MarketBuddies API</th>
    <th>MarketBuddies Worker</th>
    <th>Tranquilo Bank</th>
  </tr>
  <tr>
    <td><a href="assets/spring-initializr-marketbuddies-api.png"><img src="assets/spring-initializr-marketbuddies-api.png" alt="Spring Initializr — MarketBuddies API" width="280"></a></td>
    <td><a href="assets/spring-initializr-marketbuddies-worker.png"><img src="assets/spring-initializr-marketbuddies-worker.png" alt="Spring Initializr — MarketBuddies Worker" width="280"></a></td>
    <td><a href="assets/spring-initializr-tranquilo-bank.png"><img src="assets/spring-initializr-tranquilo-bank.png" alt="Spring Initializr — Tranquilo Bank" width="280"></a></td>
  </tr>
</table>

| Dependência | API | Worker | Bank | Para quê |
|-------------|:---:|:------:|:----:|----------|
| Spring Web | ✓ | | ✓ | Controllers e API REST |
| Spring Security | ✓ | | | Autenticação e autorização por perfil |
| Thymeleaf | ✓ | | | Telas renderizadas no servidor |
| Spring Data JPA | ✓ | | | Persistência com Hibernate |
| PostgreSQL Driver | ✓ | | | Driver JDBC |
| Validation | ✓ | | ✓ | Bean Validation em formulários e corpos JSON |
| Spring for RabbitMQ | ✓ | ✓ | | Filas do pagamento |
| Java Mail Sender | ✓ | | | E-mail de confirmação |
| HTTP Client | | ✓ | | Chamada ao Tranquilo Bank |
| Lombok | ✓ | ✓ | ✓ | Menos boilerplate |
| Spring Boot DevTools | ✓ | ✓ | ✓ | Reinício automático em desenvolvimento |

Fora do Initializr, a API usa ainda `thymeleaf-extras-springsecurity6` (mostra ou esconde elementos por perfil) e `jjwt` (gera e valida o token JWT).

---

## Estrutura do Projeto

Pacotes no plural, por camada, no padrão dos projetos do professor.

```
cp5-market-buddies/
├── marketbuddies-api/        # loja web, API REST e mensageria (porta 8080)
│   └── src/main/
│       ├── java/.../api/
│       │   ├── configs/      # segurança, RabbitMQ, propriedades e dados iniciais
│       │   ├── security/     # filtro e utilitário do JWT
│       │   ├── controllers/  # telas, /api/** e handlers de erro
│       │   ├── facades/      # fluxo do pagamento (pedido → fila → resposta)
│       │   ├── services/     # regras de negócio, carrinho, e-mail, dashboard e job de expiração
│       │   ├── producers/    # publica o pedido de pagamento
│       │   ├── consumers/    # lê as respostas do pagamento
│       │   ├── dtos/         # records de entrada e saída
│       │   ├── entities/     # tabelas
│       │   ├── enums/        # status, perfis e categorias
│       │   ├── exceptions/   # exceções de negócio
│       │   └── repositories/ # acesso ao banco
│       └── resources/
│           ├── static/       # CSS, JS e imagens
│           └── templates/    # telas Thymeleaf
│               └── email/    # template HTML do e-mail de confirmação
├── marketbuddies-worker/     # integração de pagamentos (sem porta)
│   └── src/main/java/.../worker/
│       ├── configs/          # filas, conversor JSON e cliente HTTP
│       ├── consumers/        # lê o pedido de pagamento
│       ├── clients/          # chama o Tranquilo Bank
│       ├── producers/        # publica a resposta
│       └── dtos/             # mensagens trocadas na fila
├── tranquilo-bank/           # API do banco (porta 8090)
│   └── src/main/java/.../tranquilobank/
│       ├── controllers/      # POST /api/payments/authorize
│       ├── services/         # regra de aprovação
│       └── dtos/             # pedido e resposta da autorização
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

<!-- print pendente: assets/tela-login.png -->

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

O banco **recusa** quando o total é **maior que R$ 500,00** (`tranquilo-bank.approval-limit` no `application.yaml`); caso contrário aprova e devolve um código `TB-` + 8 caracteres hexadecimais. A decisão de negócio volta sempre com `200`, aprovada ou recusada. O banco não guarda transações: cada decisão vai para o log (`Pedido 42 · R$ 33,83 · APROVADO TB-7F3A9C21`), e o registro oficial fica no pedido da api.

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

**Assunto:** `MarketBuddies · Pagamento confirmado do pedido #42`. **Corpo:** HTML montado pelo template Thymeleaf `templates/email/order-confirmation.html` (com alternativa em texto puro), com nome do cliente, itens com quantidade e preço, total, código de autorização e "Obrigado por comprar no MarketBuddies!".

> **E-mail no deploy:** o plano Trial do Railway bloqueia SMTP de saída ("SMTP is only available on the Pro plan and above", [documentação](https://docs.railway.com/reference/outbound-networking)). Por isso, no deploy o envio fica registrado como `ERROR` (timeout na porta 587), e o resto do fluxo funciona normalmente. Rodando local, o e-mail chega, como mostra o vídeo. O ADMIN pode usar "Reenviar e-mail" no dashboard.

<!-- print pendente: assets/email-confirmacao.png -->
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

O limite de aprovação (R$ 500,00) não é variável de ambiente: fica em `tranquilo-bank.approval-limit`, no `application.yaml`.

---

## Credenciais de Teste

| Perfil | E-mail | Senha |
|--------|--------|-------|
| CUSTOMER | `cliente@marketbuddies.com` | `cliente123` |
| ADMIN | exibido na tela de login e no `.txt` da entrega | exibida na tela de login e no `.txt` da entrega |

A conta de cliente é criada pelo seed (nome "Ana Souza"). A tela de login mostra as duas contas, cliente e ADMIN, no quadro "Contas de teste", com botão de copiar o e-mail e a senha.

---

## Modelo de Dados

![Diagrama ER](assets/diagrama-er.png)

<!-- print pendente: assets/banco-tabelas.png (as 5 tabelas no PostgreSQL) -->

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

<!-- print pendente: assets/postman-corpo-invalido.png -->

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

> Os JSONs acima são os de `postman/exemplos.md`. Datas, ids e códigos de autorização mudam a cada execução.

---

## Painel do RabbitMQ e Testes Internos

Painel no Railway: https://rabbitmq-production-e3dc.up.railway.app (usuário e senha no `.txt` da entrega). Local: http://localhost:15672.

- **Exchanges** — as três `direct`.

  ![Exchanges no painel do RabbitMQ](assets/rabbitmq-exchanges.png)
- **Conexões** — a api e o worker conectados.

  ![Conexões no painel do RabbitMQ](assets/rabbitmq-conexoes.png)
- **Filas** — as três filas.

  ![Filas no painel do RabbitMQ](assets/rabbitmq-filas.png)
- **Mensagem** — "Get messages" mostrando o JSON de um pedido.
  <!-- print pendente: assets/rabbitmq-mensagem.png -->

### Testes internos (RabbitMQ Monitor)

O enunciado pede "testes internos do RabbitMQ Monitor". Roteiro:

1. **Publicar pelo painel:** em `payment-request-exchange`, "Publish message" com um `PaymentRequestDto` e routing key `payment-request-routing-key`. Esperado: uma mensagem chega em `payment-response-success-queue` (valor até R$ 500,00) ou em `payment-response-error-queue` (valor acima).
   <!-- print pendente: assets/rabbitmq-publicar.png -->
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
- **Spring Web (MVC) + Thymeleaf** (telas e e-mail HTML)
- **Spring Security + JWT** (jjwt 0.12.6)
- **Spring Data JPA + Hibernate**
- **PostgreSQL**
- **Spring for RabbitMQ** (RabbitMQ 4)
- **Java Mail Sender** (Gmail SMTP)
- **Chart.js 4.4.1** (dashboard)
- **Lombok**
- **Postman**
- **Docker** e **Railway**
- **Maven**
