package br.com.fiap.marketbuddies.api.repositories;

import br.com.fiap.marketbuddies.api.entities.OrderItemEntity;
import br.com.fiap.marketbuddies.api.enums.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItemEntity, Long> {

    // P2: antes de excluir um produto já vendido, solta o vínculo dos itens.
    // O pedido antigo continua legível pela cópia de nome, categoria, preço e custo.
    @Modifying(clearAutomatically = true)
    @Query("update OrderItemEntity i set i.product = null where i.product.id = :productId")
    int detachProduct(@Param("productId") Long productId);

    // --- Dashboard da gerência (T8) ---
    // Vendas e lucro saem da foto gravada no item (unitPrice e unitCost do momento da compra),
    // nunca do custo atual do produto. Só contam pedidos APPROVED.

    interface DailyTotals {
        LocalDate getRefDate();

        BigDecimal getSales();

        BigDecimal getProfit();
    }

    interface CategoryTotal {
        Category getCategory();

        BigDecimal getSales();
    }

    interface OrderProfit {
        Long getOrderId();

        BigDecimal getProfit();
    }

    @Query("""
            select cast(o.createdAt as LocalDate) as refDate,
                   sum(i.unitPrice * i.quantity) as sales,
                   sum((i.unitPrice - i.unitCost) * i.quantity) as profit
              from OrderItemEntity i join i.order o
             where o.status = br.com.fiap.marketbuddies.api.enums.OrderStatus.APPROVED
               and o.createdAt >= :from and o.createdAt < :to
             group by cast(o.createdAt as LocalDate)
            """)
    List<DailyTotals> approvedTotalsByDay(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    @Query("""
            select i.category as category, sum(i.unitPrice * i.quantity) as sales
              from OrderItemEntity i join i.order o
             where o.status = br.com.fiap.marketbuddies.api.enums.OrderStatus.APPROVED
               and o.createdAt >= :from and o.createdAt < :to
             group by i.category
            """)
    List<CategoryTotal> approvedSalesByCategory(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    @Query("""
            select i.order.id as orderId, sum((i.unitPrice - i.unitCost) * i.quantity) as profit
              from OrderItemEntity i
             where i.order.id in :orderIds
             group by i.order.id
            """)
    List<OrderProfit> profitByOrder(@Param("orderIds") Collection<Long> orderIds);
}
