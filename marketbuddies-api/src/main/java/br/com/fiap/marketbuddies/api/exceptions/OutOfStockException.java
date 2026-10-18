package br.com.fiap.marketbuddies.api.exceptions;

// Reserva de estoque falhou: 409, e o rollback desfaz o pedido inteiro.
public class OutOfStockException extends RuntimeException {

    public OutOfStockException(String message) {
        super(message);
    }
}
