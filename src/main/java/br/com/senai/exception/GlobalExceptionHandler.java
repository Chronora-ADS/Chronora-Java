package br.com.senai.exception;

import br.com.senai.exception.Auth.AuthException;
import br.com.senai.exception.NotFound.NotFoundException;
import br.com.senai.exception.Validation.EmailAlreadyExistsException;
import br.com.senai.exception.Validation.InvalidDocumentException;
import br.com.senai.exception.Validation.PhoneNumberAlreadyExistsException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {
    // 1. Trata todas as "não encontradas" → 404
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<?> handleNotFound(NotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(errorBody(ex.getMessage(), HttpStatus.NOT_FOUND));
    }

    // 2. Trata autenticação → 401
    @ExceptionHandler(AuthException.class)
    public ResponseEntity<?> handleAuth(AuthException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(errorBody(ex.getMessage(), HttpStatus.UNAUTHORIZED));
    }

    // 3. Trata conflitos (email/telefone duplicado) → 409
    @ExceptionHandler({EmailAlreadyExistsException.class, PhoneNumberAlreadyExistsException.class})
    public ResponseEntity<?> handleConflict(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(errorBody(ex.getMessage(), HttpStatus.CONFLICT));
    }

    // 4. Trata outras validações → 400
    @ExceptionHandler(InvalidDocumentException.class)
    public ResponseEntity<?> handleInvalidDoc(InvalidDocumentException ex) {
        return ResponseEntity.badRequest()
                .body(errorBody(ex.getMessage(), HttpStatus.BAD_REQUEST));
    }

    // 5. Erros de integração com Supabase → 502
    @ExceptionHandler(SupabaseIntegrationException.class)
    public ResponseEntity<?> handleSupabase(SupabaseIntegrationException ex) {
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                .body(errorBody("Serviço externo indisponível", HttpStatus.BAD_GATEWAY));
    }

    // 6. Fallback geral → 500
    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleUnexpected(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(errorBody("Erro interno", HttpStatus.INTERNAL_SERVER_ERROR));
    }

    private Map<String, Object> errorBody(String message, HttpStatus status) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", message);
        return body;
    }
}
