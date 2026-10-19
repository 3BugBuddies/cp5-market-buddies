> Respostas de exemplo da spec; trocar pelas respostas reais quando os prints forem tirados.

# Exemplos de request e response

Valores do seed: Café torrado e moído 500 g (id 7) R$ 18,71 com 7% de oferta (R$ 20,12 sem oferta); Banana prata (id 1) R$ 5,88; Maçã gala (id 2) R$ 9,81 com 10% de oferta.

## Login (`POST /api/auth/login`)

Request:

```json
{ "email": "cliente@marketbuddies.com", "password": "cliente123" }
```

Response `200`:

```json
{ "accessToken": "eyJhbGciOiJIUzI1NiJ9...", "tokenType": "Bearer", "expiresIn": 3600000 }
```

Senha errada, response `401`:

```json
{ "timestamp": "2026-10-18T20:50:00", "status": 401, "error": "Unauthorized", "message": "Usuário ou senha inválidos", "path": "/api/auth/login" }
```

## Cadastro de cliente (`POST /api/auth/signup`)

Rota pública. Cria sempre um `CUSTOMER`; a senha nunca volta na resposta.

Request:

```json
{ "fullName": "Maria Lima", "email": "maria.lima@exemplo.com", "password": "senha123", "confirmPassword": "senha123" }
```

Response `201 Created`, header `Location: /api/auth/me`:

```json
{ "id": 4, "fullName": "Maria Lima", "email": "maria.lima@exemplo.com", "role": "CUSTOMER" }
```

E-mail já cadastrado, response `409`:

```json
{ "timestamp": "2026-10-18T22:45:00.494448", "status": 409, "error": "Conflict", "message": "Já existe uma conta com esse e-mail", "path": "/api/auth/signup", "errors": { "email": "Já existe uma conta com esse e-mail" } }
```

Campos inválidos (`{ "fullName": "", "email": "x", "password": "123", "confirmPassword": "" }`), response `400`:

```json
{
  "timestamp": "2026-10-18T22:45:00.501103",
  "status": 400,
  "error": "Bad Request",
  "message": "Dados inválidos",
  "path": "/api/auth/signup",
  "errors": {
    "confirmPassword": "Confirme a senha",
    "email": "Informe um e-mail válido, como nome@dominio.com",
    "password": "A senha precisa ter no mínimo 6 caracteres",
    "fullName": "Informe seu nome completo"
  }
}
```

Senhas diferentes, response `400`:

```json
{ "timestamp": "2026-10-18T22:45:00.507687", "status": 400, "error": "Bad Request", "message": "Dados inválidos", "path": "/api/auth/signup", "errors": { "confirmPassword": "As senhas não conferem" } }
```

## Usuário logado (`GET /api/auth/me`, header `Authorization: Bearer {{token}}`)

Response `200` (sem token, `401`):

```json
{ "id": 2, "fullName": "Ana Souza", "email": "cliente@marketbuddies.com", "role": "CUSTOMER" }
```

## Detalhe do produto (`GET /api/products/7`)

Rota pública. Response `200` (id inexistente, `404`):

```json
{ "category": "GROCERY", "description": null, "discountPercentage": 7.00, "finalPrice": 18.71, "id": 7, "name": "Café torrado e moído 500 g", "onSale": true, "price": 20.12, "stock": 17 }
```

## Gestão de produtos (header `Authorization: Bearer {{adminToken}}`)

Só o ADMIN: com o token do cliente a resposta é `403`, sem token `401`. O preço nunca vai no corpo: a API calcula `price = cost × (1 + markupPercentage/100)` e `finalPrice` com o desconto.

Criar, `POST /api/products`:

```json
{ "name": "Suco de uva integral 1 L", "category": "BEVERAGES", "description": "Criado pela coleção do Postman.", "cost": 10.00, "markupPercentage": 50, "discountPercentage": 10, "stock": 30 }
```

Response `201 Created`, header `Location: /api/products/10`:

```json
{ "category": "BEVERAGES", "description": "Criado pela coleção do Postman.", "discountPercentage": 10, "finalPrice": 13.50, "id": 10, "name": "Suco de uva integral 1 L", "onSale": true, "price": 15.00, "stock": 30 }
```

Criar como cliente, response `403`:

```json
{ "timestamp": "2026-10-18T22:45:00.618", "status": 403, "error": "Forbidden", "message": "Acesso negado para o seu perfil", "path": "/api/products" }
```

Criar com nome vazio e custo zero, response `400`:

```json
{ "timestamp": "2026-10-18T22:45:00.633543", "status": 400, "error": "Bad Request", "message": "Dados inválidos", "path": "/api/products", "errors": { "cost": "O custo deve ser maior que zero.", "name": "Informe o nome do produto." } }
```

Atualizar, `PUT /api/products/10` (mesmo corpo do criar):

```json
{ "name": "Suco de uva integral 1,5 L", "category": "BEVERAGES", "description": "Criado pela coleção do Postman.", "cost": 12.00, "markupPercentage": 50, "discountPercentage": 0, "stock": 25 }
```

Response `200`:

```json
{ "category": "BEVERAGES", "description": "Criado pela coleção do Postman.", "discountPercentage": 0, "finalPrice": 18.00, "id": 10, "name": "Suco de uva integral 1,5 L", "onSale": false, "price": 18.00, "stock": 25 }
```

O campo `version` é opcional. Se vier e não for a versão atual (o produto mudou depois da leitura), response `409`:

```json
{ "timestamp": "2026-10-18T22:45:00.654475", "status": 409, "error": "Conflict", "message": "O registro mudou enquanto você editava; consulte de novo e reenvie", "path": "/api/products/10" }
```

Excluir, `DELETE /api/products/10`: response `204 No Content`, sem corpo. Itens de pedidos já vendidos perdem o vínculo com o produto e mantêm a cópia de nome e preço. Depois disso, `GET /api/products/10` responde `404`:

```json
{ "timestamp": "2026-10-18T22:45:00.690131", "status": 404, "error": "Not Found", "message": "Produto 10 não encontrado", "path": "/api/products/10" }
```

## Compra aprovada (`POST /api/orders`, header `Authorization: Bearer {{token}}`)

Request (1 café + 2 bananas):

```json
{ "items": [ { "productId": 7, "quantity": 1 }, { "productId": 1, "quantity": 2 } ] }
```

Response `202 Accepted`, header `Location: /api/orders/42`:

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

## Pedido aprovado (`GET /api/orders/42`, depois de ~2 s)

Response `200`:

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

## Pedido recusado (17 cafés + 21 maçãs = R$ 524,08)

Request `POST /api/orders`:

```json
{ "items": [ { "productId": 7, "quantity": 17 }, { "productId": 2, "quantity": 21 } ] }
```

Response `202` (o pedido nasce aguardando):

```json
{ "id": 43, "status": "AWAITING_PAYMENT", "totalAmount": 524.08, "customerEmail": "cliente@marketbuddies.com", "createdAt": "2026-10-18T20:53:00", "items": [ { "productName": "Café torrado e moído 500 g", "quantity": 17, "unitPrice": 18.71 }, { "productName": "Maçã gala (kg)", "quantity": 21, "unitPrice": 9.81 } ] }
```

`GET /api/orders/43` depois de ~2 s, response `200`:

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

## Meus pedidos (`GET /api/orders`, header `Authorization: Bearer {{token}}`)

O cliente vê só os próprios pedidos; com `{{adminToken}}`, a lista traz os de todos os clientes. Mais recentes primeiro, até 50. Response `200`:

```json
[
  {
    "authorizationCode": null,
    "createdAt": "2026-10-18T22:44:55.787318",
    "customerEmail": "cliente@marketbuddies.com",
    "id": 2,
    "items": [
      { "productName": "Maçã gala (kg)", "quantity": 21, "unitPrice": 9.81 },
      { "productName": "Café torrado e moído 500 g", "quantity": 17, "unitPrice": 18.71 }
    ],
    "origin": "API",
    "refusalReason": "Valor acima do limite de R$ 500,00",
    "respondedAt": "2026-10-18T22:44:55.976095",
    "status": "REFUSED",
    "statusEmail": null,
    "totalAmount": 524.08
  },
  {
    "authorizationCode": "TB-1A26E1B2",
    "createdAt": "2026-10-18T22:44:55.672338",
    "customerEmail": "cliente@marketbuddies.com",
    "id": 1,
    "items": [
      { "productName": "Banana prata (kg)", "quantity": 2, "unitPrice": 5.88 },
      { "productName": "Café torrado e moído 500 g", "quantity": 1, "unitPrice": 18.71 }
    ],
    "origin": "API",
    "refusalReason": null,
    "respondedAt": "2026-10-18T22:44:55.976431",
    "status": "APPROVED",
    "statusEmail": "ERROR",
    "totalAmount": 30.47
  }
]
```

## Erros da API de compras

Sem estoque (`POST /api/orders` com `quantity` 999), response `409`:

```json
{ "timestamp": "2026-10-18T20:54:00", "status": 409, "error": "Conflict", "message": "Sem estoque para Café torrado e moído 500 g (pedido 999, disponível 18)", "path": "/api/orders" }
```

Sem token (sem o header `Authorization`), response `401`:

```json
{ "timestamp": "2026-10-18T20:54:30", "status": 401, "error": "Unauthorized", "message": "Autenticação necessária", "path": "/api/orders" }
```

Corpo inválido (`{ "items": [] }`), response `400`:

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

## Tranquilo Bank (`POST /api/payments/authorize`)

Autorização aprovada, request:

```json
{ "orderId": 42, "amount": 33.83, "customerEmail": "cliente@marketbuddies.com" }
```

Response `200`:

```json
{ "orderId": 42, "approved": true, "authorizationCode": "TB-7F3A9C21", "reason": null, "processedAt": "2026-10-18T20:52:01" }
```

Autorização recusada, request com `"amount": 523.88`, response `200`:

```json
{ "orderId": 43, "approved": false, "authorizationCode": null, "reason": "Valor acima do limite de R$ 500,00", "processedAt": "2026-10-18T20:53:01" }
```

Banco com corpo inválido (sem `amount`), response `400`:

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
