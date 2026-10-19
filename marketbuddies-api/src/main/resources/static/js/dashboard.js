// Dashboard da gerência: os três gráficos com Chart.js.
// Os dados vêm do servidor em window.MB_DASHBOARD (template admin/dashboard, th:inline="javascript").
(() => {
  const data = window.MB_DASHBOARD;
  if (!data || typeof Chart === 'undefined') return;

  // Cores do design system (tokens em marketbuddies.css).
  const css = getComputedStyle(document.documentElement);
  const token = (name, fallback) => css.getPropertyValue(name).trim() || fallback;
  const blue = token('--mb-blue-700', '#1a4fa0');
  const red = token('--mb-red-600', '#e1251b');
  const ink = token('--mb-ink-600', '#4a5568');
  const line = token('--mb-line', '#dde3ec');

  const brl = new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' });
  const brlShort = (v) => 'R$ ' + Number(v).toLocaleString('pt-BR', { maximumFractionDigits: 0 });
  const toNumbers = (list) => (list || []).map(Number);

  Chart.defaults.font.family = 'Inter, system-ui, sans-serif';
  Chart.defaults.font.size = 12;
  Chart.defaults.color = ink;
  Chart.defaults.borderColor = line;
  Chart.defaults.maintainAspectRatio = false;
  Chart.defaults.animation = window.matchMedia('(prefers-reduced-motion: reduce)').matches ? false : Chart.defaults.animation;

  // Em barra horizontal (indexAxis 'y') o valor está em x.
  const valueOf = (ctx) => (ctx.chart.options.indexAxis === 'y' ? ctx.parsed.x : ctx.parsed.y);
  const moneyTooltip = { callbacks: { label: (ctx) => `${ctx.dataset.label}: ${brl.format(valueOf(ctx))}` } };

  // Vendas × lucro dos últimos 7 dias.
  new Chart(document.getElementById('chart-days'), {
    type: 'bar',
    data: {
      labels: data.dayLabels,
      datasets: [
        { label: 'Vendas', data: toNumbers(data.daySales), backgroundColor: blue, borderRadius: 3, maxBarThickness: 22 },
        { label: 'Lucro', data: toNumbers(data.dayProfit), backgroundColor: red, borderRadius: 3, maxBarThickness: 22 }
      ]
    },
    options: {
      // A legenda é HTML no cabeçalho do card, como na tela de referência.
      plugins: { legend: { display: false }, tooltip: moneyTooltip },
      scales: {
        x: { grid: { display: false } },
        y: { beginAtZero: true, ticks: { callback: brlShort } }
      }
    }
  });

  // Vendas da semana por categoria (categoria gravada no item).
  new Chart(document.getElementById('chart-categories'), {
    type: 'bar',
    data: {
      labels: data.categoryLabels,
      datasets: [{ label: 'Vendas', data: toNumbers(data.categorySales), backgroundColor: blue, borderRadius: 3, maxBarThickness: 18 }]
    },
    options: {
      indexAxis: 'y',
      plugins: { legend: { display: false }, tooltip: moneyTooltip },
      scales: {
        x: { beginAtZero: true, ticks: { callback: brlShort, maxTicksLimit: 5 } },
        y: { grid: { display: false } }
      }
    }
  });

  // Nome longo de produto encolhe o gráfico no celular: corta o rótulo do eixo (o tooltip mostra o nome inteiro).
  const shortLabel = (chart, label) => {
    const max = chart.width < 500 ? 16 : 32;
    return label.length > max ? label.slice(0, max - 1) + '…' : label;
  };

  // Estoque por produto, em vermelho quando ≤ 5.
  const low = data.stockLow || [];
  new Chart(document.getElementById('chart-stock'), {
    type: 'bar',
    data: {
      labels: data.stockLabels,
      datasets: [{
        label: 'Estoque',
        data: toNumbers(data.stockValues),
        backgroundColor: low.map((isLow) => (isLow ? red : blue)),
        borderRadius: 3,
        maxBarThickness: 16,
        minBarLength: 2
      }]
    },
    options: {
      indexAxis: 'y',
      plugins: {
        legend: { display: false },
        tooltip: { callbacks: { label: (ctx) => `${ctx.parsed.x} un.` } }
      },
      scales: {
        x: { beginAtZero: true, ticks: { precision: 0 } },
        y: {
          grid: { display: false },
          ticks: { callback(value) { return shortLabel(this.chart, this.getLabelForValue(value)); } }
        }
      }
    }
  });
})();
