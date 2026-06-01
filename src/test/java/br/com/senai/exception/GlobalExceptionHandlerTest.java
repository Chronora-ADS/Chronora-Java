package br.com.senai.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.mock.http.MockHttpInputMessage;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void deveRetornarBadRequestQuandoJsonForInvalido() {
        HttpMessageNotReadableException exception = new HttpMessageNotReadableException(
                "JSON parse error",
                new MockHttpInputMessage(new byte[0])
        );

        var response = handler.handleUnreadableBody(exception);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(400, ((Map<?, ?>) response.getBody()).get("status"));
        assertEquals("JSON inválido", ((Map<?, ?>) response.getBody()).get("message"));
    }
}
