(() => {
  const data = window.MB_DASHBOARD;
  if (!data || typeof Chart === 'undefined') return;

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

  const valueOf = (ctx) => (ctx.chart.options.indexAxis === 'y' ? ctx.parsed.x : ctx.parsed.y);
  const moneyTooltip = { callbacks: { label: (ctx) => `${ctx.dataset.label}: ${brl.format(valueOf(ctx))}` } };

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
      plugins: { legend: { display: false }, tooltip: moneyTooltip },
      scales: {
        x: { grid: { display: false } },
        y: { beginAtZero: true, ticks: { callback: brlShort } }
      }
    }
  });

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

  const shortLabel = (chart, label) => {
    const max = chart.width < 500 ? 16 : 32;
    return label.length > max ? label.slice(0, max - 1) + '…' : label;
  };

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
