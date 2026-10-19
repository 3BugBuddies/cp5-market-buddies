package br.com.fiap.marketbuddies.api.repositories;

import br.com.fiap.marketbuddies.api.entities.EmailEntity;
import br.com.fiap.marketbuddies.api.enums.StatusEmail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface EmailRepository extends JpaRepository<EmailEntity, Long> {

    // Último e-mail do pedido (statusEmail de GET /api/orders/{id} e da tela do pedido).
    Optional<EmailEntity> findFirstByOrder_IdOrderByIdDesc(Long orderId);

    // Controle de envio (GET /api/emails): os 50 mais recentes primeiro.
    List<EmailEntity> findTop50ByOrderByIdDesc();

    // --- Dashboard da gerência (T8) ---

    interface OrderEmailStatus {
        Long getOrderId();

        StatusEmail getStatusEmail();
    }

    // Status do último e-mail de cada pedido; pedido sem linha em TB_EMAILS não aparece.
    @Query("""
            select e.order.id as orderId, e.statusEmail as statusEmail
              from EmailEntity e
             where e.order.id in :orderIds
               and e.id = (select max(e2.id) from EmailEntity e2 where e2.order = e.order)
            """)
    List<OrderEmailStatus> findLatestStatusByOrder(@Param("orderIds") Collection<Long> orderIds);
}
