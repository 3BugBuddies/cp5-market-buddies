package br.com.fiap.marketbuddies.api.services;

import br.com.fiap.marketbuddies.api.dtos.DashboardDto;
import br.com.fiap.marketbuddies.api.dtos.DashboardDto.CategoryPoint;
import br.com.fiap.marketbuddies.api.dtos.DashboardDto.DayPoint;
import br.com.fiap.marketbuddies.api.dtos.DashboardDto.Kpis;
import br.com.fiap.marketbuddies.api.dtos.DashboardDto.RecentOrder;
import br.com.fiap.marketbuddies.api.dtos.DashboardDto.StockPoint;
import br.com.fiap.marketbuddies.api.enums.Category;
import br.com.fiap.marketbuddies.api.enums.OrderStatus;
import br.com.fiap.marketbuddies.api.enums.StatusEmail;
import br.com.fiap.marketbuddies.api.repositories.EmailRepository;
import br.com.fiap.marketbuddies.api.repositories.OrderItemRepository;
import br.com.fiap.marketbuddies.api.repositories.OrderItemRepository.DailyTotals;
import br.com.fiap.marketbuddies.api.repositories.OrderRepository;
import br.com.fiap.marketbuddies.api.repositories.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

// Dashboard da gerência. As somas e contagens são feitas no banco (consultas dos repositórios);
// aqui só se monta a tela: dias sem venda viram zero, percentuais e rótulos.
//
// "Hoje" é o dia do fuso padrão da JVM, o mesmo usado para gravar createdAt. No deploy a JVM sobe com
// -Duser.timezone=America/Sao_Paulo (Dockerfile), então o dia é o de Brasília.
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardService {

    static final int LOW_STOCK_LIMIT = 5;
    static final int CHART_DAYS = 7;
    static final int RECENT_ORDERS = 10;

    private static final DateTimeFormatter DAY_LABEL = DateTimeFormatter.ofPattern("dd/MM");
    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductRepository productRepository;
    private final EmailRepository emailRepository;

    public DashboardDto load() {
        LocalDate today = LocalDate.now();
        LocalDate firstDay = today.minusDays(CHART_DAYS - 1L);
        LocalDateTime from = firstDay.atStartOfDay();
        LocalDateTime to = today.plusDays(1).atStartOfDay();

        Map<LocalDate, DailyTotals> totalsByDay = orderItemRepository.approvedTotalsByDay(from, to).stream()
                .collect(Collectors.toMap(DailyTotals::getRefDate, Function.identity()));

        List<DayPoint> days = new ArrayList<>(CHART_DAYS);
        for (LocalDate day = firstDay; !day.isAfter(today); day = day.plusDays(1)) {
            DailyTotals totals = totalsByDay.get(day);
            days.add(new DayPoint(day.format(DAY_LABEL),
                    totals == null ? BigDecimal.ZERO : money(totals.getSales()),
                    totals == null ? BigDecimal.ZERO : money(totals.getProfit())));
        }

        return new DashboardDto(today, kpis(today, days), days, categories(from, to), stock(), recentOrders());
    }

    private Kpis kpis(LocalDate today, List<DayPoint> days) {
        DayPoint todayPoint = days.getLast();
        DayPoint yesterdayPoint = days.get(days.size() - 2);

        Map<OrderStatus, Long> todayCounts = countByStatus(today);
        long paidToday = todayCounts.getOrDefault(OrderStatus.APPROVED, 0L);
        long paidYesterday = countByStatus(today.minusDays(1)).getOrDefault(OrderStatus.APPROVED, 0L);

        BigDecimal ticketToday = average(todayPoint.sales(), paidToday);
        BigDecimal ticketYesterday = average(yesterdayPoint.sales(), paidYesterday);

        BigDecimal margin = todayPoint.sales().signum() == 0 ? null
                : todayPoint.profit().multiply(HUNDRED).divide(todayPoint.sales(), 1, RoundingMode.HALF_UP);

        ProductRepository.MarkupSummary markup = productRepository.summarizeMarkup();
        BigDecimal averageMarkup = markup.getAverage() == null ? null
                : BigDecimal.valueOf(markup.getAverage()).setScale(0, RoundingMode.HALF_UP);

        List<ProductRepository.StockRow> lowStock = productRepository.findLowStock(LOW_STOCK_LIMIT);
        long outOfStock = lowStock.stream().filter(row -> row.getStock() <= 0).count();
        String lowStockNames = lowStock.stream().map(ProductRepository.StockRow::getName)
                .collect(Collectors.joining(", "));

        return new Kpis(
                todayPoint.sales(), variation(todayPoint.sales(), yesterdayPoint.sales()),
                paidToday,
                todayCounts.getOrDefault(OrderStatus.REFUSED, 0L),
                todayCounts.getOrDefault(OrderStatus.AWAITING_PAYMENT, 0L),
                ticketToday, variation(ticketToday, ticketYesterday),
                todayPoint.profit(), margin,
                averageMarkup, markup.getTotal(),
                lowStock.size(), outOfStock, lowStockNames);
    }

    private Map<OrderStatus, Long> countByStatus(LocalDate day) {
        return orderRepository.countByStatusBetween(day.atStartOfDay(), day.plusDays(1).atStartOfDay()).stream()
                .collect(Collectors.toMap(OrderRepository.StatusCount::getStatus, OrderRepository.StatusCount::getTotal));
    }

    // Todas as categorias aparecem, mesmo sem venda, da maior para a menor.
    private List<CategoryPoint> categories(LocalDateTime from, LocalDateTime to) {
        Map<Category, BigDecimal> sales = new EnumMap<>(Category.class);
        orderItemRepository.approvedSalesByCategory(from, to)
                .forEach(row -> sales.put(row.getCategory(), money(row.getSales())));
        List<CategoryPoint> points = new ArrayList<>();
        for (Category category : Category.values()) {
            points.add(new CategoryPoint(category.getLabel(), sales.getOrDefault(category, BigDecimal.ZERO)));
        }
        points.sort(Comparator.comparing(CategoryPoint::sales).reversed());
        return points;
    }

    private List<StockPoint> stock() {
        return productRepository.findStockOverview().stream()
                .map(row -> new StockPoint(row.getName(), row.getStock(), row.getStock() <= LOW_STOCK_LIMIT))
                .toList();
    }

    private List<RecentOrder> recentOrders() {
        List<OrderRepository.RecentOrderRow> rows = orderRepository.findRecent(PageRequest.of(0, RECENT_ORDERS));
        if (rows.isEmpty()) {
            return List.of();
        }
        List<Long> ids = rows.stream().map(OrderRepository.RecentOrderRow::getId).toList();
        Map<Long, BigDecimal> profitByOrder = orderItemRepository.profitByOrder(ids).stream()
                .collect(Collectors.toMap(OrderItemRepository.OrderProfit::getOrderId, row -> money(row.getProfit())));
        Map<Long, StatusEmail> emailByOrder = emailRepository.findLatestStatusByOrder(ids).stream()
                .collect(Collectors.toMap(EmailRepository.OrderEmailStatus::getOrderId,
                        EmailRepository.OrderEmailStatus::getStatusEmail));

        return rows.stream().map(row -> {
            boolean approved = row.getStatus() == OrderStatus.APPROVED;
            StatusEmail email = emailByOrder.get(row.getId());
            return new RecentOrder(row.getId(), row.getCustomerName(), row.getCreatedAt(), row.getTotalAmount(),
                    approved ? profitByOrder.get(row.getId()) : null,
                    row.getStatus(), email,
                    // P5: o botão "Reenviar e-mail" entra com a T7; a regra de quem pode já fica aqui.
                    approved && email != StatusEmail.SENT);
        }).toList();
    }

    private static BigDecimal average(BigDecimal total, long count) {
        return count == 0 ? BigDecimal.ZERO : total.divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP);
    }

    // Variação percentual inteira contra a base; nula quando a base é zero (não há com o que comparar).
    private static Integer variation(BigDecimal current, BigDecimal base) {
        if (base == null || base.signum() == 0) {
            return null;
        }
        return current.subtract(base).multiply(HUNDRED).divide(base, 0, RoundingMode.HALF_UP).intValue();
    }

    private static BigDecimal money(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value.setScale(2, RoundingMode.HALF_UP);
    }
}
