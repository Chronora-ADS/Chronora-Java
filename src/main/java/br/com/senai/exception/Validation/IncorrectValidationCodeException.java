package br.com.senai.exception.Validation;

public class IncorrectValidationCodeException extends ValidationException {
    public IncorrectValidationCodeException(String message) {
        super(message);
    }
}
