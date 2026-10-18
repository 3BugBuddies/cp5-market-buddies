package br.com.fiap.marketbuddies.api.repositories;

import br.com.fiap.marketbuddies.api.entities.OrderItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OrderItemRepository extends JpaRepository<OrderItemEntity, Long> {

    // P2: antes de excluir um produto já vendido, solta o vínculo dos itens.
    // O pedido antigo continua legível pela cópia de nome, categoria, preço e custo.
    @Modifying(clearAutomatically = true)
    @Query("update OrderItemEntity i set i.product = null where i.product.id = :productId")
    int detachProduct(@Param("productId") Long productId);
}
