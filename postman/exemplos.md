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
