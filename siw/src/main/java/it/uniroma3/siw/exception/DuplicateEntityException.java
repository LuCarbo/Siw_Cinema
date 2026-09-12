package it.uniroma3.siw.exception;

public class DuplicateEntityException extends BusinessException {

    public DuplicateEntityException(String message) {
        super(message);
    }

    public DuplicateEntityException(String field, String message) {
        super(field, message);
    }
}
