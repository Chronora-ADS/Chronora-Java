package br.com.senai.exception.NotFound;

public class ServiceNotFoundException extends NotFoundException {
    public ServiceNotFoundException(String message) {
        super(message);
    }
}
