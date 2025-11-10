package br.com.senai.exception.NotFound;

import br.com.senai.exception.DomainException;

public abstract class NotFoundException extends DomainException {
    public NotFoundException(String message) {
        super(message);
    }
}