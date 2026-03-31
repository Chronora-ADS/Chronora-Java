package br.com.senai.exception.Validation;

public class ExpiredValidationCodeException extends ValidationException {
    public ExpiredValidationCodeException(String message) {
        super(message);
    }
}
