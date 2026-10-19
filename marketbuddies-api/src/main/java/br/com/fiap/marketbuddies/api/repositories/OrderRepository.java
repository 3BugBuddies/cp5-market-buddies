package br.com.fiap.marketbuddies.api.repositories;

import br.com.fiap.marketbuddies.api.entities.OrderEntity;
import br.com.fiap.marketbuddies.api.enums.OrderStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<OrderEntity, Long> {

    /** Update condicional: devolve 0 se o pedido já foi respondido. */
    @Modifying(clearAutomatically = true)
    @Query("""
            update OrderEntity o
               set o.status = :newStatus,
                   o.authorizationCode = :code,
                   o.refusalReason = :reason,
                   o.respondedAt = :now
             where o.id = :id
               and o.status = br.com.fiap.marketbuddies.api.enums.OrderStatus.AWAITING_PAYMENT
            """)
    int respond(@Param("id") Long id,
                @Param("newStatus") OrderStatus newStatus,
                @Param("code") String code,
                @Param("reason") String reason,
                @Param("now") LocalDateTime now);

    List<OrderEntity> findByStatusAndCreatedAtBefore(OrderStatus status, LocalDateTime limit);

    @EntityGraph(attributePaths = {"customer", "items", "items.product"})
    Optional<OrderEntity> findDetailedById(Long id);

    @EntityGraph(attributePaths = {"customer", "items"})
    List<OrderEntity> findTop50ByOrderByCreatedAtDescIdDesc();

    @EntityGraph(attributePaths = {"customer", "items"})
    List<OrderEntity> findTop50ByCustomer_IdOrderByCreatedAtDescIdDesc(Long customerId);

    interface StatusCount {
        OrderStatus getStatus();

        Long getTotal();
    }

    interface RecentOrderRow {
        Long getId();

        String getCustomerName();

        LocalDateTime getCreatedAt();

        BigDecimal getTotalAmount();

        OrderStatus getStatus();
    }

    @Query("""
            select o.status as status, count(o) as total
              from OrderEntity o
             where o.createdAt >= :from and o.createdAt < :to
             group by o.status
            """)
    List<StatusCount> countByStatusBetween(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    @Query("""
            select o.id as id, c.fullName as customerName, o.createdAt as createdAt,
                   o.totalAmount as totalAmount, o.status as status
              from OrderEntity o join o.customer c
             order by o.createdAt desc, o.id desc
            """)
    List<RecentOrderRow> findRecent(Pageable pageable);
}
