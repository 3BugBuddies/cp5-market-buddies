package br.com.fiap.marketbuddies.api.repositories;

import br.com.fiap.marketbuddies.api.entities.EmailEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EmailRepository extends JpaRepository<EmailEntity, Long> {

    // Último e-mail do pedido (statusEmail de GET /api/orders/{id} e da tela do pedido).
    Optional<EmailEntity> findFirstByOrder_IdOrderByIdDesc(Long orderId);

    // Controle de envio (GET /api/emails): os 50 mais recentes primeiro.
    List<EmailEntity> findTop50ByOrderByIdDesc();
}
