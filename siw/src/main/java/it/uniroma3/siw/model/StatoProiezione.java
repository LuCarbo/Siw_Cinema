package it.uniroma3.siw.model;

public enum StatoProiezione {
    SCHEDULED("Programmata"),
    COMPLETED("Completata"),
    CANCELLED("Annullata");

    private final String label;

    StatoProiezione(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
