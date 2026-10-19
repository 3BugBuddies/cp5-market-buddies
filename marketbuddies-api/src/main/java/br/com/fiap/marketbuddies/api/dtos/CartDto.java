package br.com.fiap.marketbuddies.api.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

// Carrinho montado para as telas de carrinho e checkout, com preço e estoque lidos do banco agora.
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartDto {

    private List<Item> items = new ArrayList<>();
    private Integer totalQuantity;
    private BigDecimal total;
    // Avisos de produtos que saíram do catálogo desde que entraram no carrinho (P4).
    private List<String> notices = new ArrayList<>();

    public boolean isEmpty() {
        return items.isEmpty();
    }

    // Algum item pede mais do que o estoque atual: o pagamento vai falhar até o cliente ajustar.
    public boolean isOverStock() {
        return items.stream().anyMatch(Item::isOverStock);
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Item {
        private Long productId;
        private String name;
        private String categoryLabel;
        private BigDecimal unitPrice;
        private BigDecimal regularPrice;
        private Boolean onSale;
        private Integer quantity;
        private Integer stock;
        private BigDecimal subtotal;

        public boolean isOverStock() {
            return quantity > stock;
        }
    }
}
