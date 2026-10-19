package br.com.fiap.marketbuddies.api.exceptions;

import lombok.Getter;

@Getter
public class FieldValidationException extends BusinessException {

    private final String field;

    public FieldValidationException(String field, String message) {
        super(message);
        this.field = field;
    }
}
