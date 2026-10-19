package br.com.fiap.marketbuddies.api.dtos;

import br.com.fiap.marketbuddies.api.entities.ProductEntity;
import br.com.fiap.marketbuddies.api.enums.Category;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.math.BigDecimal;

/** Ordem explícita: sem ela o Jackson 3 põe os componentes do record na ordem do construtor. */
@JsonPropertyOrder({"category", "description", "discountPercentage", "finalPrice", "id", "imagePath", "name", "onSale", "price",
        "stock"})
public record ProductDto(
        Long id,
        String name,
        Category category,
        String description,
        BigDecimal price,
        BigDecimal finalPrice,
        BigDecimal discountPercentage,
        Integer stock,
        Boolean onSale,
        String imagePath,
        @JsonIgnore BigDecimal cost,
        @JsonIgnore BigDecimal markupPercentage,
        @JsonIgnore Long version) {

    public static ProductDto from(ProductEntity product, boolean withInternalData) {
        return new ProductDto(
                product.getId(),
                product.getName(),
                product.getCategory(),
                product.getDescription(),
                product.getPrice(),
                product.getFinalPrice(),
                product.getDiscountPercentage(),
                product.getStock(),
                product.isOnSale(),
                product.getImagePath(),
                withInternalData ? product.getCost() : null,
                withInternalData ? product.getMarkupPercentage() : null,
                withInternalData ? product.getVersion() : null);
    }
}
