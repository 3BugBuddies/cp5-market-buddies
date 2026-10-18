package br.com.fiap.marketbuddies.api.enums;

public enum Category {
    PRODUCE("Hortifruti"),
    CLEANING("Limpeza"),
    BEVERAGES("Bebidas"),
    BAKERY("Padaria"),
    GROCERY("Mercearia");

    private final String label;

    Category(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
