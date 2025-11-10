package br.com.senai.exception.Validation;

public class PhoneNumberAlreadyExistsException extends ValidationException {
    public PhoneNumberAlreadyExistsException(String phone) {
        super("O número de telefone " + phone + " já está em uso.");
    }
}
