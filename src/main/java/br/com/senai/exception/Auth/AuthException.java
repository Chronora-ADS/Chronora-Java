package br.com.senai.exception.Auth;

import br.com.senai.exception.DomainException;

public class AuthException extends DomainException {
    public AuthException(String message) {
        super(message);
    }
}
