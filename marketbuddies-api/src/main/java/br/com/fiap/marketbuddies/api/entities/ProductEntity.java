package br.com.fiap.marketbuddies.api.entities;

import br.com.fiap.marketbuddies.api.enums.Category;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Data
@Entity
@Table(name = "TB_PRODUCTS")
public class ProductEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 80)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Category category;

    @Column(length = 500)
    private String description;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal cost;

    // 40.00 = 40%
    @Column(nullable = false, precision = 7, scale = 2)
    private BigDecimal markupPercentage;

    // Sempre calculado pela aplicação (custo x (1 + markup)); nunca digitado.
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal price;

    // 0 = sem oferta.
    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal discountPercentage = BigDecimal.ZERO;

    @Column(nullable = false)
    private Integer stock;

    @Column(length = 120)
    private String imagePath;

    // Trava otimista (P3): reserve/release no repositório também incrementam a versão.
    @Version
    private Long version;

    @PrePersist
    @PreUpdate
    void calculatePrice() {
        price = cost.multiply(BigDecimal.ONE.add(markupPercentage.movePointLeft(2)))
                .setScale(2, RoundingMode.HALF_UP);
    }

    // Preço cobrado, já com a oferta.
    @Transient
    public BigDecimal getFinalPrice() {
        return price.multiply(BigDecimal.ONE.subtract(discountPercentage.movePointLeft(2)))
                .setScale(2, RoundingMode.HALF_UP);
    }

    @Transient
    public boolean isOnSale() {
        return discountPercentage.signum() > 0;
    }
}
