package br.com.fiap.marketbuddies.api.entities;

import br.com.fiap.marketbuddies.api.enums.Category;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.math.BigDecimal;

// Nome, categoria, preço e custo são a foto do momento da compra (D8):
// editar ou excluir o produto depois não muda o que foi vendido.
@Data
@Entity
@Table(name = "TB_ORDER_ITEMS")
public class OrderItemEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private OrderEntity order;

    // Fica nulo quando o produto é excluído (P2).
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private ProductEntity product;

    @Column(nullable = false, length = 80)
    private String productName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Category category;

    @Column(nullable = false)
    private Integer quantity;

    // Preço final do dia (com oferta).
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal unitPrice;

    // Custo do dia (D8).
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal unitCost;
}
