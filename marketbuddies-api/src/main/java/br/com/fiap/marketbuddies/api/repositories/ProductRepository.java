package br.com.fiap.marketbuddies.api.repositories;

import br.com.fiap.marketbuddies.api.entities.ProductEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductRepository extends JpaRepository<ProductEntity, Long> {

    // Reserva atômica (D17): só baixa se houver estoque. Devolve 0 quando falta,
    // e quem chama lança OutOfStockException para o rollback desfazer o pedido inteiro.
    @Modifying(clearAutomatically = true)
    @Query("""
            update ProductEntity p
               set p.stock = p.stock - :qty, p.version = p.version + 1
             where p.id = :id and p.stock >= :qty
            """)
    int reserve(@Param("id") Long id, @Param("qty") int qty);

    // Devolução de estoque (pedido recusado ou expirado).
    @Modifying(clearAutomatically = true)
    @Query("""
            update ProductEntity p
               set p.stock = p.stock + :qty, p.version = p.version + 1
             where p.id = :id
            """)
    int release(@Param("id") Long id, @Param("qty") int qty);
}
