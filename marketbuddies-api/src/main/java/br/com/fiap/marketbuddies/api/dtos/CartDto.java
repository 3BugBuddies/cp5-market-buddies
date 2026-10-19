package br.com.fiap.marketbuddies.api.dtos;

import java.math.BigDecimal;
import java.util.List;

// Carrinho montado para as telas de carrinho e checkout, com preço e estoque lidos do banco agora.
// notices: avisos de produtos que saíram do catálogo desde que entraram no carrinho (P4).
public record CartDto(
        List<Item> items,
        Integer totalQuantity,
        BigDecimal total,
        List<String> notices) {

    public boolean isEmpty() {
        return items.isEmpty();
    }

    // Algum item pede mais do que o estoque atual: o pagamento vai falhar até o cliente ajustar.
    public boolean isOverStock() {
        return items.stream().anyMatch(Item::isOverStock);
    }

    public record Item(
            Long productId,
            String name,
            String categoryLabel,
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
