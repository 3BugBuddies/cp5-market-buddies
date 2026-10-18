# MarketBuddies — Design System

> Documento de referência para implementar a interface web (Thymeleaf + Spring Security) do **MarketBuddies**, um mercado express. Direção visual: **varejo clássico** (azul + vermelho, sensação de hipermercado), com identidade **própria** — não usar logo, nome, fonte ou elementos gráficos de nenhuma marca real.

---

## 0. Instruções para quem for implementar (Claude Code)

1. **CSS puro, sem frameworks** (sem Bootstrap/Tailwind). Um único arquivo: `src/main/resources/static/css/marketbuddies.css`.
2. Todos os valores visuais vêm dos **tokens da seção 2**. Nunca escrever hex, px de espaçamento ou raio soltos dentro de componentes — sempre `var(--mb-*)`.
3. Todas as páginas usam os **fragments da seção 5** (`head`, `navbar`, `footer`, `alerts`). Nada de navbar duplicada por página.
4. Prefixo de classes: `mb-` (padrão BEM leve: `.mb-btn`, `.mb-btn--danger`, `.mb-card__title`).
5. Ícones: **Tabler Icons webfont** via CDN (`<i class="ti ti-shopping-cart"></i>`). Sem emoji na interface.
6. Textos da UI em **português do Brasil**, em sentence case ("Adicionar produto", não "Adicionar Produto").
7. Precisa da dependência `org.thymeleaf.extras:thymeleaf-extras-springsecurity6` para `sec:authorize`.
8. Ao terminar, rodar o checklist da seção 9.

---

## 1. Marca

### 1.1 Nome
**MarketBuddies** — sempre junto, com M e B maiúsculos. No logotipo, "Market" em azul e "Buddies" em vermelho.

### 1.2 Símbolo
Dois círculos sobrepostos (os dois "buddies"): azul à esquerda, vermelho à direita com leve transparência, e um **sorriso branco** na interseção — os dois amigos juntos formam um rosto feliz.

Os arquivos já existem e vão em `src/main/resources/static/img/` (copiar, não redesenhar):

| Arquivo | Uso |
|---|---|
| `logo.svg` | Logo completo (símbolo + texto), sobre fundo claro: login, signup, cards |
| `logo-white.svg` | Logo completo para fundo azul: navbar, hero, painel do login |
| `logo-mark.svg` | Só o símbolo: footer, estados vazios, decorações |
| `favicon.svg` | Ícone da aba (`<link rel="icon" type="image/svg+xml">`) |

**`static/img/logo-mark.svg`** (referência)
```svg
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 40 28" width="40" height="28" role="img" aria-label="MarketBuddies">
  <circle cx="14" cy="14" r="12" fill="#1A4FA0"/>
  <circle cx="26" cy="14" r="12" fill="#E1251B" fill-opacity="0.9"/>
  <path d="M16.5 17 Q20 20.5 23.5 17" fill="none" stroke="#FFFFFF" stroke-width="2" stroke-linecap="round"/>
</svg>
```

### 1.3 Logotipo (HTML)
Usar o SVG completo como imagem (o texto já está dentro do SVG):
```html
<a th:href="@{/}" class="mb-logo" aria-label="MarketBuddies — início">
  <img th:src="@{/img/logo-white.svg}" alt="MarketBuddies" height="32">
</a>
```
- Navbar e superfícies azuis → `logo-white.svg` ("Market" branco, "Buddies" em `--mb-red-300`).
- Fundos claros → `logo.svg` ("Market" azul, "Buddies" vermelho).
- Altura: 32px na navbar, 40px no login/signup, 24px no footer. Nunca distorcer (sempre definir só a altura).
- Área de respiro mínima: metade da altura do símbolo em todos os lados.
- Não recolorir, não girar, não trocar a ordem das cores dos círculos.

### 1.4 Tom de voz
Próximo e direto, como um atendente de mercado de bairro. Ex.: "Bem-vindo de volta!", "Seu carrinho está vazio. Bora às compras?", "Produto salvo".

---

## 2. Tokens

Colar no topo de `marketbuddies.css`:

```css
:root {
  /* Marca */
  --mb-blue-900: #0E2F66;
  --mb-blue-700: #1A4FA0;   /* primária */
  --mb-blue-600: #2360C2;   /* hover da primária */
  --mb-blue-100: #E3ECFA;
  --mb-blue-50:  #F2F6FD;

  --mb-red-700: #B81D15;    /* hover do vermelho */
  --mb-red-600: #E1251B;    /* destaque / CTA de compra */
  --mb-red-300: #FF8A82;
  --mb-red-100: #FDE6E4;

  /* Neutros */
  --mb-ink-900: #1B2333;    /* texto principal */
  --mb-ink-600: #4A5568;    /* texto secundário */
  --mb-ink-400: #8A94A6;    /* placeholder, legenda */
  --mb-line:    #DDE3EC;    /* bordas */
  --mb-bg:      #F5F7FB;    /* fundo da página */
  --mb-surface: #FFFFFF;    /* cards, tabelas, forms */

  /* Semânticas */
  --mb-success: #1E8E4F;  --mb-success-bg: #E4F4EA;
  --mb-warning: #B7791F;  --mb-warning-bg: #FDF3DC;
  --mb-danger:  #C53030;  --mb-danger-bg:  #FDE8E8;
  --mb-info:    var(--mb-blue-700); --mb-info-bg: var(--mb-blue-100);

  /* Tipografia */
  --mb-font: "Inter", system-ui, -apple-system, "Segoe UI", Roboto, sans-serif;
  --mb-fs-xs: 0.75rem;   /* 12 */
  --mb-fs-sm: 0.875rem;  /* 14 */
  --mb-fs-md: 1rem;      /* 16 — corpo */
  --mb-fs-lg: 1.25rem;   /* 20 */
  --mb-fs-xl: 1.75rem;   /* 28 */
  --mb-fs-2xl: 2.5rem;   /* 40 — hero */
  --mb-fw-regular: 400;
  --mb-fw-medium: 500;
  --mb-fw-bold: 700;     /* só títulos e preço */

  /* Espaçamento (escala de 4px) */
  --mb-space-1: 4px;  --mb-space-2: 8px;  --mb-space-3: 12px;
  --mb-space-4: 16px; --mb-space-5: 24px; --mb-space-6: 32px;
  --mb-space-7: 48px; --mb-space-8: 64px;

  /* Forma */
  --mb-radius-sm: 6px;    /* inputs, botões */
  --mb-radius-md: 10px;   /* cards */
  --mb-radius-lg: 16px;   /* hero, modais */
  --mb-radius-pill: 999px;
  --mb-shadow-sm: 0 1px 2px rgba(27, 35, 51, 0.06), 0 1px 3px rgba(27, 35, 51, 0.08);
  --mb-shadow-md: 0 4px 12px rgba(27, 35, 51, 0.10);
  --mb-focus: 0 0 0 3px rgba(35, 96, 194, 0.35);

  /* Layout */
  --mb-container: 1200px;
  --mb-navbar-h: 64px;
  --mb-transition: 150ms ease;
}
```

Fonte (no fragment `head`):
```html
<link rel="preconnect" href="https://fonts.googleapis.com">
<link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;700&display=swap" rel="stylesheet">
<link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/@tabler/icons-webfont@latest/dist/tabler-icons.min.css">
```

### 2.1 Regras de uso de cor
| Uso | Cor |
|---|---|
| Navbar, botão primário, links | `--mb-blue-700` |
| CTA de compra ("Adicionar ao carrinho", "Finalizar compra"), preço em promoção, badge de oferta | `--mb-red-600` |
| Excluir / ações destrutivas | `--mb-danger` (não usar o vermelho da marca, para não confundir "comprar" com "apagar") |
| Fundo da página | `--mb-bg` |
| Texto | `--mb-ink-900`; secundário `--mb-ink-600` |

**Regra de ouro:** no máximo **um** botão vermelho de marca por tela.

---

## 3. Base e layout

```css
*, *::before, *::after { box-sizing: border-box; }
body { margin: 0; font-family: var(--mb-font); font-size: var(--mb-fs-md); line-height: 1.6;
       color: var(--mb-ink-900); background: var(--mb-bg); min-height: 100vh;
       display: flex; flex-direction: column; }
main { flex: 1; }
a { color: var(--mb-blue-700); text-decoration: none; }
a:hover { text-decoration: underline; }
h1, h2, h3 { line-height: 1.25; margin: 0 0 var(--mb-space-4); }
h1 { font-size: var(--mb-fs-xl); font-weight: var(--mb-fw-bold); }
h2 { font-size: var(--mb-fs-lg); font-weight: var(--mb-fw-bold); }
h3 { font-size: var(--mb-fs-md); font-weight: var(--mb-fw-medium); }
:focus-visible { outline: none; box-shadow: var(--mb-focus); }
```

- `.mb-container` → `max-width: var(--mb-container); margin: 0 auto; padding: 0 var(--mb-space-4);`
- `.mb-page` → `padding: var(--mb-space-6) 0;`
- `.mb-page-header` → flex, `justify-content: space-between`, `align-items: center`, título à esquerda e ação principal à direita, `margin-bottom: var(--mb-space-5)`.
- `.mb-grid` → `display: grid; gap: var(--mb-space-4); grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));`
- Breakpoints: `640px` (mobile) e `960px` (tablet). Mobile-first; a página nunca pode ter scroll horizontal.

---

## 4. Componentes

### 4.1 Botões — `.mb-btn`
Base: `display:inline-flex; align-items:center; gap:var(--mb-space-2); height:40px; padding:0 var(--mb-space-4); border-radius:var(--mb-radius-sm); font-weight:var(--mb-fw-medium); font-size:var(--mb-fs-sm); border:1px solid transparent; cursor:pointer; transition:background var(--mb-transition);`

| Variante | Fundo | Texto | Borda | Hover |
|---|---|---|---|---|
| `.mb-btn--primary` | `--mb-blue-700` | branco | — | `--mb-blue-600` |
| `.mb-btn--buy` | `--mb-red-600` | branco | — | `--mb-red-700` |
| `.mb-btn--secondary` | `--mb-surface` | `--mb-blue-700` | `--mb-line` | fundo `--mb-blue-50` |
| `.mb-btn--ghost` | transparente | `--mb-ink-600` | — | fundo `--mb-blue-50` |
| `.mb-btn--danger` | `--mb-surface` | `--mb-danger` | `--mb-danger` | fundo `--mb-danger-bg` |

Modificadores: `.mb-btn--sm` (32px de altura), `.mb-btn--block` (largura 100%). Ícone sempre à esquerda do texto. Botões só de ícone (ex.: editar na tabela) precisam de `aria-label` e `title`.

### 4.2 Formulários
```html
<div class="mb-field">
  <label class="mb-label" for="nome">Nome do produto</label>
  <input class="mb-input" id="nome" th:field="*{nome}" placeholder="Banana prata (kg)">
  <p class="mb-field__error" th:if="${#fields.hasErrors('nome')}" th:errors="*{nome}"></p>
</div>
```
- `.mb-input`, `.mb-select`, `.mb-textarea`: altura 40px (textarea livre), `padding: 0 var(--mb-space-3)`, borda 1px `--mb-line`, `border-radius: var(--mb-radius-sm)`, fundo branco. Foco: borda `--mb-blue-600` + `--mb-focus`.
- Estado de erro: adicionar `.mb-input--error` (borda `--mb-danger`) quando `#fields.hasErrors`.
- `.mb-label`: `--mb-fs-sm`, peso 500, `margin-bottom: var(--mb-space-1)`.
- `.mb-field__error`: `--mb-fs-xs`, cor `--mb-danger`.
- `.mb-field__hint`: `--mb-fs-xs`, cor `--mb-ink-400`.
- `.mb-form-actions`: flex, gap `--mb-space-3`, alinhado à direita, `margin-top: var(--mb-space-5)`.
- Input de senha: botão ghost com `ti-eye` / `ti-eye-off` para mostrar/ocultar (JS inline curto).

### 4.3 Card — `.mb-card`
Fundo `--mb-surface`, borda 1px `--mb-line`, `border-radius: var(--mb-radius-md)`, `padding: var(--mb-space-5)`, `box-shadow: var(--mb-shadow-sm)`. Partes: `.mb-card__title`, `.mb-card__body`, `.mb-card__footer` (borda superior `--mb-line`, flex, gap).

### 4.4 Card de produto — `.mb-product`
Para a vitrine (visão do cliente).
```
┌───────────────────────┐
│ [badge Oferta]        │
│   imagem/ícone 4:3    │  fundo --mb-blue-50, ícone ti-basket 48px se não houver imagem
├───────────────────────┤
│ Categoria (xs, ink-400)│
│ Nome (md, 500)         │
│ R$ 9,90 (lg, 700)      │  em oferta: preço vermelho + preço antigo riscado ink-400
│ Estoque: 12 un. (xs)   │
│ [Adicionar ao carrinho]│  .mb-btn--buy .mb-btn--block
└───────────────────────┘
```
Hover: `box-shadow: var(--mb-shadow-md)` e `transform: translateY(-2px)`. Sem estoque: card com `opacity` só na imagem, badge "Esgotado" e botão desabilitado.

Formatação de preço em Thymeleaf: `th:text="'R$ ' + ${#numbers.formatDecimal(p.preco, 1, 'POINT', 2, 'COMMA')}"`.

### 4.5 Tabela — `.mb-table`
Para a gestão de produtos (CRUD).
- Envolver em `.mb-table-wrap` (`overflow-x: auto`, borda, raio md, fundo branco).
- `th`: fundo `--mb-blue-50`, cor `--mb-ink-600`, `--mb-fs-xs`, peso 500, maiúsculas **não**; `padding: var(--mb-space-3) var(--mb-space-4)`.
- `td`: mesmo padding, borda inferior `--mb-line`; linha em hover com fundo `--mb-bg`.
- Colunas numéricas (preço, estoque) alinhadas à direita.
- Última coluna "Ações": botões `.mb-btn--sm` — Ver (`ti-eye`, ghost), Editar (`ti-edit`, secondary), Excluir (`ti-trash`, danger).
- Estado vazio: linha única com `.mb-empty`.

### 4.6 Badges — `.mb-badge`
`display:inline-flex; padding: 2px var(--mb-space-2); border-radius: var(--mb-radius-pill); font-size: var(--mb-fs-xs); font-weight: 500;`
| Variante | Fundo | Texto | Uso |
|---|---|---|---|
| `--offer` | `--mb-red-600` | branco | Oferta |
| `--admin` | `--mb-blue-700` | branco | Perfil ADMIN |
| `--client` | `--mb-blue-100` | `--mb-blue-900` | Perfil CLIENTE |
| `--success` | `--mb-success-bg` | `--mb-success` | Em estoque, Pago, E-mail enviado |
| `--warning` | `--mb-warning-bg` | `--mb-warning` | Estoque baixo (≤ 5), Pendente |
| `--danger` | `--mb-danger-bg` | `--mb-danger` | Esgotado, Recusado |

### 4.7 Alertas — `.mb-alert`
Faixa com ícone à esquerda, `border-radius: var(--mb-radius-sm)`, `padding: var(--mb-space-3) var(--mb-space-4)`, borda esquerda **sem** arredondamento não — usar borda completa 1px na cor semântica e fundo `*-bg`.
Variantes: `--success` (`ti-circle-check`), `--danger` (`ti-alert-circle`), `--info` (`ti-info-circle`), `--warning` (`ti-alert-triangle`).
Exibidos pelo fragment `alerts` a partir de flash attributes `sucesso` / `erro` e dos parâmetros `?error`, `?logout`, `?registered` do login.

### 4.8 Navbar — `.mb-navbar`
- Altura `--mb-navbar-h`, fundo `--mb-blue-700`, texto branco, `position: sticky; top: 0; z-index: 10`.
- Faixa vermelha de 4px na base (`border-bottom: 4px solid var(--mb-red-600)`) — é a assinatura visual da marca.
- Esquerda: logo (versão branca). Centro: links. Direita: área do usuário.
- Links: branco a 85% de opacidade no texto via cor `#DCE6F7`; ativo/hover em branco com sublinhado de 2px.
- Área do usuário logado: ícone `ti-user-circle`, nome, badge do perfil, botão "Sair" (form POST para `/logout`, porque o Spring Security exige POST com CSRF).
- Visitante: "Entrar" (ghost branco) + "Criar conta" (`.mb-btn--buy`).
- Mobile (< 640px): links colapsam num botão `ti-menu-2` que alterna a classe `.is-open`.

### 4.9 Footer — `.mb-footer`
Fundo `--mb-ink-900`, texto `--mb-ink-400`, `--mb-fs-sm`, padding `--mb-space-6` vertical. Logo pequeno + "© 2026 MarketBuddies — projeto acadêmico FIAP".

### 4.10 Estado vazio — `.mb-empty`
Centralizado, ícone 48px `--mb-ink-400`, título `--mb-fs-lg`, uma linha de texto e um CTA. Ex.: ícone `ti-basket-off`, "Nenhum produto cadastrado", botão "Adicionar produto".

### 4.11 Confirmação de exclusão
Exclusão via `<form method="post">` (nunca GET). Antes de enviar, `onsubmit="return confirm('Excluir ' + this.dataset.nome + '? Essa ação não pode ser desfeita.')"`. Simples e suficiente para o escopo.

### 4.12 KPI card — `.mb-kpi` (dashboard, Parte 2)
Card com rótulo (`--mb-fs-sm`, `--mb-ink-600`), valor (`--mb-fs-xl`, 700) e variação opcional (badge success/danger). Grid de 4 colunas no desktop, 2 no tablet, 1 no mobile.

---

## 5. Estrutura Thymeleaf

```
src/main/resources/
├── static/
│   ├── css/marketbuddies.css
│   └── img/ logo.svg, logo-white.svg, logo-mark.svg, favicon.svg
└── templates/
    ├── fragments/layout.html      ← head, navbar, alerts, footer
    ├── index.html                 ← landing (pública)
    ├── auth/login.html            ← login customizado (pública)
    ├── auth/signup.html           ← cadastro (pública)
    ├── produtos/vitrine.html      ← CLIENTE e ADMIN: catálogo em cards
    ├── produtos/detalhe.html      ← CLIENTE e ADMIN: um produto (o "consultar produto" da tarefa)
    ├── produtos/lista.html        ← ADMIN: tabela CRUD
    ├── produtos/form.html         ← ADMIN: criar/editar (mesmo template)
    ├── admin/dashboard.html       ← ADMIN: KPIs (base para a Parte 2)
    └── error/403.html, error/404.html
```

### 5.1 `fragments/layout.html`
```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org"
      xmlns:sec="http://www.thymeleaf.org/extras/spring-security">

<head th:fragment="head(title)">
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <title th:text="${title} + ' · MarketBuddies'">MarketBuddies</title>
  <link rel="icon" type="image/svg+xml" th:href="@{/img/favicon.svg}">
  <!-- fontes e ícones da seção 2 -->
  <link rel="stylesheet" th:href="@{/css/marketbuddies.css}">
</head>

<header th:fragment="navbar" class="mb-navbar">
  <!-- logo / links / área do usuário -->
  <!-- Links por perfil:
       sec:authorize="isAuthenticated()"   → Produtos (vitrine)
       sec:authorize="hasRole('ADMIN')"     → Gerenciar produtos, Dashboard
       sec:authorize="isAnonymous()"        → Entrar, Criar conta
       Nome do usuário: sec:authentication="name" -->
</header>

<div th:fragment="alerts">
  <!-- .mb-alert para ${sucesso} e ${erro} -->
</div>

<footer th:fragment="footer" class="mb-footer"> … </footer>
</html>
```

### 5.2 Esqueleto de toda página
```html
<!DOCTYPE html>
<html lang="pt-BR" xmlns:th="http://www.thymeleaf.org">
<head th:replace="~{fragments/layout :: head('Produtos')}"></head>
<body>
  <header th:replace="~{fragments/layout :: navbar}"></header>
  <main class="mb-page">
    <div class="mb-container">
      <div th:replace="~{fragments/layout :: alerts}"></div>
      <!-- conteúdo -->
    </div>
  </main>
  <footer th:replace="~{fragments/layout :: footer}"></footer>
</body>
</html>
```

---

## 6. Especificação das telas

### 6.1 `index.html` — landing (requisito: página base deve ser `index.html`)
1. **Hero**: fundo `--mb-blue-700` com raio lg dentro do container, texto branco. Título "Seu mercado express, do seu jeito." (`--mb-fs-2xl`), subtítulo de uma linha, dois botões: "Criar conta" (`--buy`) e "Já tenho conta" (secondary). À direita, composição grande dos dois círculos do símbolo (decorativa, `aria-hidden`).
2. **Faixa de benefícios**: 3 cards pequenos com ícone (`ti-truck-delivery` "Entrega rápida", `ti-discount-2` "Ofertas toda semana", `ti-shield-lock` "Compra segura").
3. **Categorias**: chips/pílulas (Hortifruti, Limpeza, Bebidas, Padaria, Mercearia) — apenas visuais.
4. Footer.

### 6.2 `auth/login.html` — login customizado (requisito: diferente do padrão do Spring Security)
- Layout em **duas colunas** no desktop: esquerda com painel azul da marca (logo branco grande, frase "Bem-vindo de volta!", círculos decorativos); direita com o formulário em `.mb-card` de até 400px. No mobile, só o formulário com o logo em cima.
- Form: `th:action="@{/login}" method="post"`, campos com `name="username"` (rótulo "E-mail") e `name="password"` (rótulo "Senha"). Se a config usar outros nomes via `usernameParameter`, manter consistente.
- Alertas: `th:if="${param.error}"` → danger "E-mail ou senha incorretos."; `${param.logout}` → info "Você saiu da sua conta."; `${param.registered}` → success "Conta criada! Agora é só entrar."
- Botão "Entrar" `.mb-btn--primary .mb-btn--block`.
- Abaixo: "Ainda não tem conta? **Criar conta**" → `/signup` (requisito: redirecionar quem não tem login para o cadastro).
- Mesmo layout de duas colunas é reaproveitado no signup.

### 6.3 `auth/signup.html` — cadastro (salva em `TDS_Users_Mercado`)
- Campos: Nome completo, E-mail, Senha (com hint "Mínimo de 6 caracteres"), Confirmar senha.
- `th:object="${usuario}"`, erros por campo com `.mb-field__error`.
- Novos cadastros entram como **CLIENTE**; o ADMIN vem de um seed (`data.sql` ou `CommandLineRunner`). A tela não oferece escolha de perfil.
- Sucesso → `redirect:/login?registered`.

### 6.4 `produtos/vitrine.html` — CLIENTE e ADMIN
- Page header "Produtos" + campo de busca (`GET /produtos?busca=`) e select de categoria.
- `.mb-grid` de `.mb-product`. Clique no card → detalhe.
- Botão "Adicionar ao carrinho" já estilizado (ponte para a Parte 2).

### 6.5 `produtos/detalhe.html` — consulta de um produto (requisito central da Parte 1)
- Breadcrumb "Produtos › Nome".
- Duas colunas: imagem/ícone grande à esquerda; à direita categoria, nome (h1), preço grande, badge de estoque, descrição, código/ID em `--mb-ink-400`.
- CLIENTE: botão `--buy`. ADMIN: botões "Editar" e "Excluir" (via `sec:authorize="hasRole('ADMIN')"`).

### 6.6 `produtos/lista.html` — ADMIN, CRUD em tabela
- Page header "Gerenciar produtos" + botão primário "Adicionar produto" (`ti-plus`).
- `.mb-table` com colunas: ID, Nome, Categoria, Preço, Estoque (badge warning se ≤ 5, danger se 0), Ações (Ver / Editar / Excluir).
- Todos os 4 verbos do CRUD visíveis como **links/botões** (exigência da tarefa).

### 6.7 `produtos/form.html` — criar e editar
- Título dinâmico: `th:text="${produto.id == null} ? 'Novo produto' : 'Editar produto'"`.
- Card com grid de 2 colunas no desktop: Nome, Categoria (select), Preço, Estoque, Descrição (textarea ocupando 2 colunas), "Em oferta" (checkbox).
- Ações: "Cancelar" (ghost, volta para a lista) e "Salvar produto" (primary).
- Sucesso → redirect para a lista com flash `sucesso`: "Produto salvo".

### 6.8 `admin/dashboard.html` — ADMIN (base para a Parte 2)
- 4 `.mb-kpi`: Vendas hoje, Pedidos, Ticket médio, Produtos com estoque baixo.
- Gráficos com **Chart.js** (CDN): vendas por dia (linha, `--mb-blue-700`), vendas por categoria (barras, azul), estoque por produto (barras, vermelho apenas nos itens abaixo do mínimo, resto azul).
- Tabela "Últimas compras" com status do pagamento (badge) e status do e-mail (badge).
- Paleta dos gráficos, nesta ordem: `#1A4FA0`, `#E1251B`, `#2360C2`, `#FF8A82`, `#8A94A6`.

### 6.9 Erros
- `error/403.html`: ícone `ti-lock`, "Essa área é só para administradores.", botão "Voltar para a loja". Configurar `accessDeniedPage("/403")` ou usar o template padrão `error/403`.
- `error/404.html`: ícone `ti-basket-off`, "Não encontramos essa página.", botão "Ir para o início".

---

## 7. Perfis e navegação

| Rota | Público | CLIENTE | ADMIN |
|---|---|---|---|
| `/`, `/login`, `/signup`, `/css/**`, `/img/**` | ✓ | ✓ | ✓ |
| `/produtos`, `/produtos/{id}` | — | ✓ | ✓ |
| `/admin/produtos/**` (lista, novo, editar, excluir) | — | — | ✓ |
| `/admin/dashboard` | — | — | ✓ |

Redirecionamento pós-login por perfil (requisito: "após o login, redirecionar para a tela com acesso restrito"): ADMIN → `/admin/dashboard`, CLIENTE → `/produtos` (via `AuthenticationSuccessHandler`).

A navbar e os botões de ação precisam refletir essa tabela com `sec:authorize` — o usuário nunca deve ver um link que leva a um 403.

---

## 8. Acessibilidade e qualidade

- Contraste mínimo AA: texto branco só sobre `--mb-blue-700`, `--mb-red-600` ou mais escuros.
- Todo input tem `<label for>`. Ícones decorativos com `aria-hidden="true"`.
- `lang="pt-BR"` no `<html>`.
- Foco visível em todos os elementos interativos (`--mb-focus`).
- Nada de estilo inline nos templates, exceto valores dinâmicos inevitáveis.

---

## 9. Checklist de aceite

- [ ] `marketbuddies.css` contém os tokens da seção 2 e nenhum hex fora do `:root`.
- [ ] Todas as páginas usam os fragments `head`, `navbar`, `alerts` e `footer`.
- [ ] `index.html` é a landing e abre sem login.
- [ ] Login é a tela customizada (nunca aparece o form padrão do Spring Security).
- [ ] Visitante sem conta chega no signup a partir do login; após cadastro volta ao login com aviso de sucesso.
- [ ] Logout funciona via POST e mostra o aviso.
- [ ] CLIENTE não vê links de admin; acessar `/admin/**` como CLIENTE mostra a página 403 estilizada.
- [ ] CRUD completo acessível por botões/links na interface.
- [ ] Consulta de produto mostra os dados vindos do PostgreSQL na tela de detalhe.
- [ ] Layout funciona em 375px de largura sem scroll horizontal.
- [ ] Tirar prints de: landing, login, signup, vitrine, detalhe, lista CRUD, form, 403 — para o README.
