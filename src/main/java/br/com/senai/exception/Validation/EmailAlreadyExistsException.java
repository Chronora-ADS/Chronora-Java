package br.com.senai.exception.Validation;

public class EmailAlreadyExistsException extends ValidationException {
    public EmailAlreadyExistsException(String email) {
        super("O e-mail " + email + " já está em uso.");
    }
}
