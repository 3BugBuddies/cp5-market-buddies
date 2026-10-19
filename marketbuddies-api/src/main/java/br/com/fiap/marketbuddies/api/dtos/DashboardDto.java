package br.com.fiap.marketbuddies.api.dtos;

import br.com.fiap.marketbuddies.api.enums.OrderStatus;
import br.com.fiap.marketbuddies.api.enums.StatusEmail;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public record DashboardDto(
        LocalDate today,
        Kpis kpis,
        List<DayPoint> days,
        List<CategoryPoint> categories,
        List<StockPoint> stock,
        List<RecentOrder> recentOrders) {

    public record Kpis(
            BigDecimal salesToday,
            Integer salesVsYesterdayPct,
            long paidToday,
            long refusedToday,
            long awaitingToday,
            BigDecimal averageTicket,
            Integer ticketVsYesterdayPct,
            BigDecimal profitToday,
            BigDecimal profitMarginPct,
            BigDecimal averageMarkupPct,
            long productCount,
            long lowStockCount,
            long outOfStockCount,
            String lowStockNames) {
    }

    public record DayPoint(String label, BigDecimal sales, BigDecimal profit) {
    }

    public record CategoryPoint(String label, BigDecimal sales) {
    }

    public record StockPoint(String name, int stock, boolean low) {
    }

    public record RecentOrder(
            Long id,
            String customerName,
            LocalDateTime createdAt,
            BigDecimal total,
            BigDecimal profit,
            OrderStatus status,
            StatusEmail emailStatus,
            boolean canResendEmail) {
    }

    public Map<String, Object> charts() {
        Map<String, Object> charts = new LinkedHashMap<>();
        charts.put("dayLabels", days.stream().map(DayPoint::label).toList());
        charts.put("daySales", days.stream().map(DayPoint::sales).toList());
        charts.put("dayProfit", days.stream().map(DayPoint::profit).toList());
        charts.put("categoryLabels", categories.stream().map(CategoryPoint::label).toList());
        charts.put("categorySales", categories.stream().map(CategoryPoint::sales).toList());
        charts.put("stockLabels", stock.stream().map(StockPoint::name).toList());
        charts.put("stockValues", stock.stream().map(StockPoint::stock).toList());
        charts.put("stockLow", stock.stream().map(StockPoint::low).toList());
        return charts;
    }
}
