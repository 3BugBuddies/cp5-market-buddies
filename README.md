# MarketBuddies — CheckPoint 5 | Java Advanced

Mercado express com loja web autenticada (Spring Security + JWT + Thymeleaf + PostgreSQL) e pagamento assíncrono pelo **Tranquilo Bank** via RabbitMQ, com e-mail de confirmação e dashboard da gerência. Entrega única das **Partes I e II** do CheckPoint 5 da disciplina de Java Advanced (TDS) — FIAP.

- **Parte I:** loja web com landing, login e cadastro próprios, perfis CUSTOMER e ADMIN, CRUD de produtos pelo ADMIN e página 403 estilizada, tudo no PostgreSQL.
- **Parte II:** carrinho, checkout, pedido de pagamento publicado no RabbitMQ, integração com o Tranquilo Bank, resposta assíncrona, e-mail de confirmação registrado no banco e dashboard da gerência.

---

## Índice

- [Deploy](#deploy)
- [Integrantes do Grupo](#integrantes-do-grupo)
- [Arquitetura](#arquitetura)
- [Configuração do Projeto — Spring Initializr](#configuração-do-projeto--spring-initializr)
- [Estrutura do Projeto](#estrutura-do-projeto)
- [Como Executar](#como-executar)
- [Segurança — Spring Security + JWT](#segurança--spring-security--jwt)
- [Tratamento de Erros](#tratamento-de-erros)
- [Mensageria — RabbitMQ](#mensageria--rabbitmq)
- [E-mail de Confirmação](#e-mail-de-confirmação)
- [Variáveis de Ambiente](#variáveis-de-ambiente)
- [Credenciais de Teste](#credenciais-de-teste)
- [Modelo de Dados](#modelo-de-dados)
- [Telas da Aplicação](#telas-da-aplicação)
- [Testes com Postman](#testes-com-postman)
- [Painel do RabbitMQ e Testes Internos](#painel-do-rabbitmq-e-testes-internos)
- [Dashboard da Gerência](#dashboard-da-gerência)
- [Tecnologias Utilizadas](#tecnologias-utilizadas)

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

<!-- print pendente: descomentar quando os prints existirem
<table>
  <tr>
    <td align="center" valign="top"><a href="assets/railway-servicos.png"><img src="assets/railway-servicos.png" alt="Railway: os 5 serviços" width="420"></a><br><sub>Railway: os 5 serviços</sub></td>
    <td align="center" valign="top"><a href="assets/railway-deploy.png"><img src="assets/railway-deploy.png" alt="Landing no domínio público" width="420"></a><br><sub>Landing no domínio público</sub></td>
  </tr>
</table>
-->

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

<a href="assets/tela-login.png"><img src="assets/tela-login.png" alt="Tela de login com as contas de teste" width="640"></a>

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

O log do worker com um pedido aprovado e outro recusado está na grade do [Painel do RabbitMQ](#painel-do-rabbitmq-e-testes-internos).

---

## E-mail de Confirmação

Enviado pelo **Gmail** (SMTP `smtp.gmail.com:587`, STARTTLS, senha de app) **somente quando o pagamento é aprovado**. Pedido recusado não gera e-mail.

Como no `EmailService` do professor (`RabbitmqSpringEmail`), cada tentativa é gravada em `TB_EMAILS` (`EmailEntity`: `emailFrom`, `emailTo`, `subject`, `text`, `sendDateEmail`, `statusEmail`):

- envio ok: `SENT`;
- falha do SMTP (`MailException`): `ERROR` e a mensagem em `error_message`; o erro nunca volta para a fila nem desfaz o pagamento aprovado.

O envio acontece depois que o status do pedido foi gravado, para não segurar conexão do banco durante o SMTP. Se o e-mail falhar, o dashboard mostra o badge de falha e o ADMIN pode usar "Reenviar e-mail".

**Assunto:** `MarketBuddies · Pagamento confirmado do pedido #42`. **Corpo:** HTML montado pelo template Thymeleaf `templates/email/order-confirmation.html` (com alternativa em texto puro), com nome do cliente, itens com quantidade e preço, total, código de autorização e "Obrigado por comprar no MarketBuddies!".

> **E-mail no deploy:** o plano Trial do Railway bloqueia SMTP de saída ("SMTP is only available on the Pro plan and above", [documentação](https://docs.railway.com/reference/outbound-networking)). Por isso, no deploy o envio fica registrado como `ERROR` (timeout na porta 587), e o resto do fluxo funciona normalmente. Rodando local, o e-mail chega, como mostra o vídeo. O ADMIN pode usar "Reenviar e-mail" no dashboard.

<!-- print pendente: descomentar quando os prints existirem
<table>
  <tr>
    <td align="center" valign="top"><a href="assets/email-confirmacao.png"><img src="assets/email-confirmacao.png" alt="E-mail recebido no Gmail" width="420"></a><br><sub>E-mail recebido no Gmail</sub></td>
    <td align="center" valign="top"><a href="assets/banco-emails.png"><img src="assets/banco-emails.png" alt="TB_EMAILS com SENT e ERROR" width="420"></a><br><sub>TB_EMAILS com SENT e ERROR</sub></td>
  </tr>
</table>
-->

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

## Telas da Aplicação

Prints do deploy no Railway, na ordem do fluxo de compra e depois da gerência. Clique numa imagem para ver em tamanho real.

<table>
  <tr>
    <td align="center" valign="top"><a href="assets/tela-landing.png"><img src="assets/tela-landing.png" alt="Landing page" width="280"></a><br><sub>Landing page</sub></td>
    <td align="center" valign="top"><a href="assets/tela-cadastro.png"><img src="assets/tela-cadastro.png" alt="Cadastro com erro de validação" width="280"></a><br><sub>Cadastro com erro de validação</sub></td>
    <td align="center" valign="top"><a href="assets/tela-vitrine.png"><img src="assets/tela-vitrine.png" alt="Vitrine com fotos e filtros" width="280"></a><br><sub>Vitrine com fotos e filtros</sub></td>
  </tr>
  <tr>
    <td align="center" valign="top"><a href="assets/tela-detalhe.png"><img src="assets/tela-detalhe.png" alt="Detalhe do produto em oferta" width="280"></a><br><sub>Detalhe do produto em oferta</sub></td>
    <td align="center" valign="top"><a href="assets/tela-carrinho-preview.png"><img src="assets/tela-carrinho-preview.png" alt="Preview do carrinho no hover" width="280"></a><br><sub>Preview do carrinho no hover</sub></td>
    <td align="center" valign="top"><a href="assets/tela-carrinho.png"><img src="assets/tela-carrinho.png" alt="Carrinho do visitante" width="280"></a><br><sub>Carrinho do visitante</sub></td>
  </tr>
  <tr>
    <td align="center" valign="top"><a href="assets/tela-checkout.png"><img src="assets/tela-checkout.png" alt="Revisar e pagar" width="280"></a><br><sub>Revisar e pagar</sub></td>
    <td align="center" valign="top"><a href="assets/tela-pedido-aprovado.png"><img src="assets/tela-pedido-aprovado.png" alt="Pagamento confirmado" width="280"></a><br><sub>Pagamento confirmado</sub></td>
    <td align="center" valign="top"><a href="assets/tela-pedido-recusado.png"><img src="assets/tela-pedido-recusado.png" alt="Pagamento recusado" width="280"></a><br><sub>Pagamento recusado</sub></td>
  </tr>
  <tr>
    <td align="center" valign="top"><a href="assets/tela-meus-pedidos.png"><img src="assets/tela-meus-pedidos.png" alt="Meus pedidos (cliente)" width="280"></a><br><sub>Meus pedidos (cliente)</sub></td>
    <td align="center" valign="top"><a href="assets/tela-gestao.png"><img src="assets/tela-gestao.png" alt="Gestão de produtos (ADMIN)" width="280"></a><br><sub>Gestão de produtos (ADMIN)</sub></td>
    <td align="center" valign="top"><a href="assets/tela-modal-editar.png"><img src="assets/tela-modal-editar.png" alt="Modal de edição com preço calculado" width="280"></a><br><sub>Modal de edição com preço calculado</sub></td>
  </tr>
  <tr>
    <td align="center" valign="top"><a href="assets/tela-admin-pedidos.png"><img src="assets/tela-admin-pedidos.png" alt="Pedidos da gerência com filtro" width="280"></a><br><sub>Pedidos da gerência com filtro</sub></td>
    <td align="center" valign="top"><a href="assets/tela-403.png"><img src="assets/tela-403.png" alt="Página 403 para o cliente" width="280"></a><br><sub>Página 403 para o cliente</sub></td>
    <td align="center" valign="top"><a href="assets/tela-dashboard.png"><img src="assets/tela-dashboard.png" alt="Dashboard da gerência" width="280"></a><br><sub>Dashboard da gerência</sub></td>
    <td align="center" valign="top"><a href="assets/tela-celular.png"><img src="assets/tela-celular.png" alt="Vitrine no celular (390 px)" width="280"></a><br><sub>Vitrine no celular (390 px)</sub></td>
  </tr>
</table>

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
| `{{adminPassword}}` | vazia | Senha do ADMIN: copie do quadro "Contas de teste" da tela de login |
| `{{token}}` / `{{adminToken}}` / `{{orderId}}` / `{{productId}}` | vazias | Preenchidas pelos scripts de teste |

O login do cliente já vai com a conta de exemplo (`cliente@marketbuddies.com` / `cliente123`), e o do ADMIN com o e-mail `3bugbuddies@gmail.com`.

3. Rode a coleção na ordem, ou pelo **Collection Runner**: a pasta **1. Autenticação** grava os tokens usados no resto.

### Endpoints da API

| Pasta | Requests |
|-------|----------|
| 1. Autenticação | `POST /api/auth/signup` (cadastro, 201) · cadastro com e-mail repetido (409) · `POST /api/auth/login` cliente e ADMIN (200) · login inválido (401) · `GET /api/auth/me` |
| 2. Produtos | `GET /api/products` · `GET /api/products/{id}` · `POST /api/products` (ADMIN, 201, preço calculado) · criar como cliente (403) · criar inválido (400) · `PUT /api/products/{id}` · `DELETE /api/products/{id}` (204) · detalhe do excluído (404) |
| 3. Compras | `POST /api/orders` aprovada e recusada (202) · `GET /api/orders/{id}` · `GET /api/orders` (meus pedidos; ADMIN vê todos) · sem estoque (409) · sem token (401) · corpo inválido (400) |
| 4. E-mails | `GET /api/emails` (ADMIN) |
| 5. Tranquilo Bank | `POST /api/payments/authorize` aprovado, recusado e inválido |

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

**2. Login inválido** — mesma request com senha errada, resposta `401`.

```json
{ "timestamp": "2026-10-18T20:50:00", "status": 401, "error": "Unauthorized", "message": "Usuário ou senha inválidos", "path": "/api/auth/login" }
```

**3. Listar produtos** — `GET {{api}}/api/products`, público, resposta `200` com a lista de produtos (com preço, preço final e desconto; sem custo nem markup).

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

**7. Compra sem estoque** — `POST {{api}}/api/orders` com `quantity` 999, resposta `409`; nenhum item baixa.

```json
{ "timestamp": "2026-10-18T20:54:00", "status": 409, "error": "Conflict", "message": "Sem estoque para Café torrado e moído 500 g (pedido 999, disponível 18)", "path": "/api/orders" }
```

**8. Compra sem token** — `POST {{api}}/api/orders` sem o header `Authorization`, resposta `401`.

```json
{ "timestamp": "2026-10-18T20:54:30", "status": 401, "error": "Unauthorized", "message": "Autenticação necessária", "path": "/api/orders" }
```

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

**11. Tranquilo Bank — aprovado** — `POST {{bank}}/api/payments/authorize`, resposta `200`.

```json
{ "orderId": 42, "amount": 33.83, "customerEmail": "cliente@marketbuddies.com" }
```

```json
{ "orderId": 42, "approved": true, "authorizationCode": "TB-7F3A9C21", "reason": null, "processedAt": "2026-10-18T20:52:01" }
```

**12. Tranquilo Bank — recusado** — mesma request com `"amount": 523.88`, resposta `200`.

```json
{ "orderId": 43, "approved": false, "authorizationCode": null, "reason": "Valor acima do limite de R$ 500,00", "processedAt": "2026-10-18T20:53:01" }
```

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

**Collection Runner** — todos os testes da coleção passando.

> Os JSONs acima são os de `postman/exemplos.md`. Datas, ids e códigos de autorização mudam a cada execução.

### Prints

Executados no Postman contra o deploy. Clique numa imagem para ver em tamanho real. No print de e-mails, o `errorMessage` mostra o timeout do SMTP, que o plano Trial do Railway bloqueia.

<table>
  <tr>
    <td align="center" valign="top"><a href="assets/postman-cadastro.png"><img src="assets/postman-cadastro.png" alt="Cadastro de cliente (201)" width="280"></a><br><sub>Cadastro de cliente (201)</sub></td>
    <td align="center" valign="top"><a href="assets/postman-cadastro-duplicado.png"><img src="assets/postman-cadastro-duplicado.png" alt="E-mail duplicado (409)" width="280"></a><br><sub>E-mail duplicado (409)</sub></td>
    <td align="center" valign="top"><a href="assets/postman-login.png"><img src="assets/postman-login.png" alt="Login do cliente (200)" width="280"></a><br><sub>Login do cliente (200)</sub></td>
  </tr>
  <tr>
    <td align="center" valign="top"><a href="assets/postman-me.png"><img src="assets/postman-me.png" alt="Me (200)" width="280"></a><br><sub>Me (200)</sub></td>
    <td align="center" valign="top"><a href="assets/postman-produtos.png"><img src="assets/postman-produtos.png" alt="Listar produtos (200)" width="280"></a><br><sub>Listar produtos (200)</sub></td>
    <td align="center" valign="top"><a href="assets/postman-produto-detalhe.png"><img src="assets/postman-produto-detalhe.png" alt="Detalhe do produto (200)" width="280"></a><br><sub>Detalhe do produto (200)</sub></td>
  </tr>
  <tr>
    <td align="center" valign="top"><a href="assets/postman-produto-invalido.png"><img src="assets/postman-produto-invalido.png" alt="Criar produto inválido (400)" width="280"></a><br><sub>Criar produto inválido (400)</sub></td>
    <td align="center" valign="top"><a href="assets/postman-produto-criar.png"><img src="assets/postman-produto-criar.png" alt="Criar produto, ADMIN (201)" width="280"></a><br><sub>Criar produto, ADMIN (201)</sub></td>
    <td align="center" valign="top"><a href="assets/postman-produto-criado.png"><img src="assets/postman-produto-criado.png" alt="Detalhe do produto criado (200)" width="280"></a><br><sub>Detalhe do produto criado (200)</sub></td>
  </tr>
  <tr>
    <td align="center" valign="top"><a href="assets/postman-produto-excluir.png"><img src="assets/postman-produto-excluir.png" alt="Excluir produto, ADMIN (204)" width="280"></a><br><sub>Excluir produto, ADMIN (204)</sub></td>
    <td align="center" valign="top"><a href="assets/postman-produto-excluido.png"><img src="assets/postman-produto-excluido.png" alt="Detalhe do excluído (404)" width="280"></a><br><sub>Detalhe do excluído (404)</sub></td>
    <td align="center" valign="top"><a href="assets/postman-compra-aprovada.png"><img src="assets/postman-compra-aprovada.png" alt="Compra aprovada (202)" width="280"></a><br><sub>Compra aprovada (202)</sub></td>
  </tr>
  <tr>
    <td align="center" valign="top"><a href="assets/postman-compra-recusada.png"><img src="assets/postman-compra-recusada.png" alt="Compra acima de R$ 500 (202)" width="280"></a><br><sub>Compra acima de R$ 500 (202)</sub></td>
    <td align="center" valign="top"><a href="assets/postman-meus-pedidos.png"><img src="assets/postman-meus-pedidos.png" alt="Meus pedidos (200)" width="280"></a><br><sub>Meus pedidos (200)</sub></td>
    <td align="center" valign="top"><a href="assets/postman-emails.png"><img src="assets/postman-emails.png" alt="E-mails enviados, ADMIN (200)" width="280"></a><br><sub>E-mails enviados, ADMIN (200)</sub></td>
  </tr>
</table>

---

## Painel do RabbitMQ e Testes Internos

Painel no Railway: https://rabbitmq-production-e3dc.up.railway.app (usuário e senha no `.txt` da entrega). Local: http://localhost:15672.

Clique numa imagem para ver em tamanho real.

<table>
  <tr>
    <td align="center" valign="top"><a href="assets/rabbitmq-exchanges.png"><img src="assets/rabbitmq-exchanges.png" alt="Exchanges: as três direct" width="280"></a><br><sub>Exchanges: as três direct</sub></td>
    <td align="center" valign="top"><a href="assets/rabbitmq-conexoes.png"><img src="assets/rabbitmq-conexoes.png" alt="Conexões: api e worker" width="280"></a><br><sub>Conexões: api e worker</sub></td>
    <td align="center" valign="top"><a href="assets/rabbitmq-filas.png"><img src="assets/rabbitmq-filas.png" alt="Filas: as três filas" width="280"></a><br><sub>Filas: as três filas</sub></td>
  </tr>
  <tr>
    <td align="center" valign="top"><a href="assets/rabbitmq-mensagem.png"><img src="assets/rabbitmq-mensagem.png" alt="Get messages: JSON do pedido na fila" width="280"></a><br><sub>Get messages: JSON do pedido na fila</sub></td>
    <td align="center" valign="top"><a href="assets/rabbitmq-publicar.png"><img src="assets/rabbitmq-publicar.png" alt="Teste interno: Publish message" width="280"></a><br><sub>Teste interno: Publish message</sub></td>
    <td align="center" valign="top"><a href="assets/worker-log.png"><img src="assets/worker-log.png" alt="Log do worker: aprovado e recusado" width="280"></a><br><sub>Log do worker: aprovado e recusado</sub></td>
  </tr>
</table>

### Testes internos (RabbitMQ Monitor)

O enunciado pede "testes internos do RabbitMQ Monitor". Roteiro:

1. **Publicar pelo painel:** em `payment-request-exchange`, "Publish message" com um `PaymentRequestDto` e routing key `payment-request-routing-key`. Esperado: uma mensagem chega em `payment-response-success-queue` (valor até R$ 500,00) ou em `payment-response-error-queue` (valor acima).
   O print "Teste interno: Publish message" na grade acima mostra esse passo em produção.
2. **Banco parado:** com o `tranquilo-bank` desligado, publicar de novo. Esperado: depois de 3 tentativas, uma mensagem em `payment-response-error-queue` com "Banco indisponível".
3. **Resposta repetida:** reenviar na fila de sucesso a resposta de um pedido já aprovado. Esperado: nada muda na tela e nenhuma linha nova no banco (log "resposta ignorada").
4. **Compra de ponta a ponta:** comprar pela tela e acompanhar os contadores das três filas.

---

## Dashboard da Gerência

Tela `/admin/dashboard` (ADMIN), com Chart.js. Os dados vêm do banco; o lucro usa o custo gravado no item (foto do momento).

**KPIs:** vendas de hoje; pedidos pagos hoje (com recusados e aguardando); ticket médio; lucro de hoje e % das vendas; markup médio do catálogo; produtos com estoque ≤ 5.

**Gráficos:** vendas × lucro dos últimos 7 dias; vendas da semana por categoria; estoque por produto (vermelho quando ≤ 5).

**Últimas 10 compras:** tabela com badge de pagamento e de e-mail e o botão "Reenviar e-mail" para pedido aprovado sem e-mail ou com `ERROR`.

<a href="assets/tela-dashboard.png"><img src="assets/tela-dashboard.png" alt="Dashboard da gerência no deploy" width="640"></a>

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
