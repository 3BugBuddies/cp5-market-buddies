package br.com.fiap.marketbuddies.api.dtos;

import java.math.BigDecimal;
import java.util.List;

public record CartDto(
        List<Item> items,
        Integer totalQuantity,
        BigDecimal total,
        List<String> notices) {

    public boolean isEmpty() {
        return items.isEmpty();
    }

    public boolean isOverStock() {
        return items.stream().anyMatch(Item::isOverStock);
    }

    public record Item(
            Long productId,
            String name,
            String categoryLabel,
            String imagePath,
            BigDecimal unitPrice,
            BigDecimal regularPrice,
            Boolean onSale,
            Integer quantity,
            Integer stock,
            BigDecimal subtotal) {

        public boolean isOverStock() {
            return quantity > stock;
        }
    }
}
