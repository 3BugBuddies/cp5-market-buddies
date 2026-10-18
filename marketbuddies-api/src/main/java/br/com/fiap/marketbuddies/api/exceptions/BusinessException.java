package br.com.fiap.marketbuddies.api.exceptions;

// Regra de negócio violada (ex.: ADMIN não compra): 422.
public class BusinessException extends RuntimeException {

    public BusinessException(String message) {
        super(message);
    }
}
