package br.com.fiap.marketbuddies.api.exceptions;

// Recurso inexistente (produto, pedido, cliente): 404.
public class NotFoundException extends RuntimeException {

    public NotFoundException(String message) {
        super(message);
    }
}
