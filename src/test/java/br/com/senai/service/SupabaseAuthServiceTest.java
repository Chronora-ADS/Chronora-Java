package br.com.senai.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import br.com.senai.exception.Auth.AuthException;
import br.com.senai.repository.UserRepository;
import br.com.senai.service.auth.AuthService;
import br.com.senai.service.auth.SupabaseAuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@ExtendWith(MockitoExtension.class)
class SupabaseAuthServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuthService authService;

    @Test
    void deveTratarInvalidGrantDoSupabaseComoCredenciaisInvalidas() {
        SupabaseAuthService service = new SupabaseAuthService(
                restTemplate,
                new ObjectMapper(),
                userRepository,
                authService,
                "https://chronora.supabase.co",
                "anon-key",
                "service-role"
        );
        HttpClientErrorException exception = HttpClientErrorException.create(
                HttpStatus.BAD_REQUEST,
                "Bad Request",
                HttpHeaders.EMPTY,
                "{\"error\":\"invalid_grant\",\"error_description\":\"Invalid login credentials\"}"
                        .getBytes(StandardCharsets.UTF_8),
                StandardCharsets.UTF_8
        );

        when(restTemplate.exchange(
                eq("https://chronora.supabase.co/auth/v1/token?grant_type=password"),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(String.class)
        )).thenThrow(exception);

        assertThrows(
                AuthException.class,
                () -> service.signIn("ana@chronora.com", "senha-errada")
        );
    }
}
