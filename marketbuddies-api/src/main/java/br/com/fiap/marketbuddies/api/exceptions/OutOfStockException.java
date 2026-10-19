package br.com.fiap.marketbuddies.api.exceptions;

public class OutOfStockException extends RuntimeException {

    public OutOfStockException(String message) {
        super(message);
    }
}
