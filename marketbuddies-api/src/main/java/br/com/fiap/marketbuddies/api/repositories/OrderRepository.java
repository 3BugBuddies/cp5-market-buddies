package br.com.fiap.marketbuddies.api.repositories;

import br.com.fiap.marketbuddies.api.entities.OrderEntity;
import br.com.fiap.marketbuddies.api.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface OrderRepository extends JpaRepository<OrderEntity, Long> {

    // Idempotência: só muda o pedido que ainda aguarda pagamento. Quem recebe 1 segue
    // (e-mail ou devolução de estoque); resposta repetida ou concorrente recebe 0 e é ignorada.
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

    // Pedidos parados em AWAITING_PAYMENT, para o job de expiração (P1).
    List<OrderEntity> findByStatusAndCreatedAtBefore(OrderStatus status, LocalDateTime limit);
}
