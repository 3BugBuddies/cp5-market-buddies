package br.com.fiap.marketbuddies.api.exceptions;

import lombok.Getter;

// Regra de negócio que pertence a um campo do formulário (ex.: e-mail já cadastrado).
// O controller da tela transforma em erro no campo; fora de tela cai no handler de BusinessException (422).
@Getter
public class FieldValidationException extends BusinessException {

    private final String field;

    public FieldValidationException(String field, String message) {
        super(message);
        this.field = field;
    }
}
