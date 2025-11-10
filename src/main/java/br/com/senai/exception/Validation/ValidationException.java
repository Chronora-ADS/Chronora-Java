package br.com.senai.exception.Validation;

import br.com.senai.exception.DomainException;

public abstract class ValidationException extends DomainException {
    public ValidationException(String message) {
        super(message);
    }
}
