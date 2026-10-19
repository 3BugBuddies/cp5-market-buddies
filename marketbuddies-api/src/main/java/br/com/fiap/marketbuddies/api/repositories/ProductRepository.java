package br.com.fiap.marketbuddies.api.repositories;

import br.com.fiap.marketbuddies.api.entities.ProductEntity;
import br.com.fiap.marketbuddies.api.enums.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface ProductRepository extends JpaRepository<ProductEntity, Long> {

    // Vitrine: busca por nome (vazio = todos), com ou sem filtro de categoria.
    // Dois métodos derivados em vez de um JPQL com ":category is null", que o PostgreSQL não tipa.
    List<ProductEntity> findByNameContainingIgnoreCaseOrderByIdAsc(String name);

    List<ProductEntity> findByNameContainingIgnoreCaseAndCategoryOrderByIdAsc(String name, Category category);

    // Ofertas da semana na landing: com desconto e com estoque.
    List<ProductEntity> findTop4ByDiscountPercentageGreaterThanAndStockGreaterThanOrderByDiscountPercentageDescIdAsc(
            BigDecimal discount, Integer stock);

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

    // --- Dashboard da gerência (T8) ---

    interface StockRow {
        String getName();

        Integer getStock();
    }

    interface MarkupSummary {
        Double getAverage();

        Long getTotal();
    }

    @Query("select avg(p.markupPercentage) as average, count(p) as total from ProductEntity p")
    MarkupSummary summarizeMarkup();

    @Query("select p.name as name, p.stock as stock from ProductEntity p order by p.stock desc, p.name")
    List<StockRow> findStockOverview();

    @Query("""
            select p.name as name, p.stock as stock
              from ProductEntity p
             where p.stock <= :limit
             order by p.stock, p.name
            """)
    List<StockRow> findLowStock(@Param("limit") int limit);
}
