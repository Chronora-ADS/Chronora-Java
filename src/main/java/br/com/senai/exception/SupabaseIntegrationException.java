package br.com.senai.exception;

public class SupabaseIntegrationException extends RuntimeException {
    public SupabaseIntegrationException(String message, Throwable cause) {
        super(message, cause);
    }
}
