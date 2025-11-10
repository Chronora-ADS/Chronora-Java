package br.com.senai.service;

import br.com.senai.exception.Auth.AuthException;
import br.com.senai.exception.SupabaseIntegrationException;
import br.com.senai.exception.Validation.EmailAlreadyExistsException;
import br.com.senai.model.DTO.SupabaseAuthResponseDTO;
import br.com.senai.model.DTO.SupabaseUserDTO;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SupabaseAuthService {

    @Value("${supabase.url}")
    private String supabaseUrl;

    @Value("${supabase.anon-key}")
    private String supabaseAnonKey;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    private static final String USER_ENDPOINT = "/auth/v1/user";
    private static final String SIGNUP_ENDPOINT = "/auth/v1/signup";
    private static final String TOKEN_ENDPOINT = "/auth/v1/token?grant_type=password";

    /**
     * Valida um token JWT do Supabase
     */
    public SupabaseUserDTO validateToken(String token) {
        try {
            String url = supabaseUrl + USER_ENDPOINT;
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + token);
            headers.set("apikey", supabaseAnonKey);
            headers.setContentType(MediaType.APPLICATION_JSON);

            ResponseEntity<String> response = restTemplate.exchange(
                    url, HttpMethod.GET, new HttpEntity<>(headers), String.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                JsonNode userData = objectMapper.readTree(response.getBody());
                return SupabaseUserDTO.builder()
                        .id(userData.get("id").asText())
                        .email(userData.get("email").asText())
                        .phone(userData.has("phone") ? userData.get("phone").asText() : null)
                        .createdAt(userData.get("created_at").asText())
                        .build();
            } else if (response.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                throw new AuthException("Token inválido ou expirado");
            } else {
                throw new SupabaseIntegrationException("Resposta inesperada do Supabase: " + response.getStatusCode(), null);
            }
        } catch (HttpClientErrorException.Unauthorized e) {
            throw new AuthException("Token inválido");
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            throw new SupabaseIntegrationException("Erro na chamada ao Supabase", e);
        } catch (RestClientException e) {
            throw new SupabaseIntegrationException("Falha de conexão com o Supabase", e);
        } catch (Exception e) {
            throw new SupabaseIntegrationException("Erro inesperado ao validar token", e);
        }
    }

    /**
     * Registra um usuário no Supabase
     */
    public SupabaseUserDTO signUp(String email, String password, Map<String, Object> userMetadata) {
        try {
            String url = supabaseUrl + SIGNUP_ENDPOINT;

            Map<String, Object> body = new HashMap<>();
            body.put("email", email);
            body.put("password", password);
            body.put("data", userMetadata);

            HttpHeaders headers = new HttpHeaders();
            headers.set("apikey", supabaseAnonKey);
            headers.setContentType(MediaType.APPLICATION_JSON);

            ResponseEntity<String> response = restTemplate.exchange(
                    url, HttpMethod.POST, new HttpEntity<>(body, headers), String.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                JsonNode userData = objectMapper.readTree(response.getBody()).get("user");
                return SupabaseUserDTO.builder()
                        .id(userData.get("id").asText())
                        .email(userData.get("email").asText())
                        .phone(userData.has("phone") ? userData.get("phone").asText() : null)
                        .createdAt(userData.get("created_at").asText())
                        .build();
            } else if (response.getStatusCode() == HttpStatus.CONFLICT) {
                throw new EmailAlreadyExistsException(email);
            } else {
                throw new SupabaseIntegrationException("Erro no cadastro no Supabase: " + response.getStatusCode(), null);
            }
        } catch (HttpClientErrorException.Conflict e) {
            throw new EmailAlreadyExistsException(email);
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            throw new SupabaseIntegrationException("Erro ao cadastrar no Supabase", e);
        } catch (RestClientException e) {
            throw new SupabaseIntegrationException("Falha de rede com o Supabase", e);
        } catch (Exception e) {
            throw new SupabaseIntegrationException("Erro inesperado no cadastro", e);
        }
    }

    /**
     * Login no Supabase
     */
    public SupabaseAuthResponseDTO signIn(String email, String password) {
        try {
            String url = supabaseUrl + TOKEN_ENDPOINT;

            Map<String, Object> body = new HashMap<>();
            body.put("email", email);
            body.put("password", password);

            HttpHeaders headers = new HttpHeaders();
            headers.set("apikey", supabaseAnonKey);
            headers.setContentType(MediaType.APPLICATION_JSON);

            ResponseEntity<String> response = restTemplate.exchange(
                    url, HttpMethod.POST, new HttpEntity<>(body, headers), String.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                JsonNode json = objectMapper.readTree(response.getBody());
                JsonNode userNode = json.get("user");

                SupabaseUserDTO user = SupabaseUserDTO.builder()
                        .id(userNode.get("id").asText())
                        .email(userNode.get("email").asText())
                        .phone(userNode.has("phone") ? userNode.get("phone").asText() : null)
                        .createdAt(userNode.get("created_at").asText())
                        .build();

                return SupabaseAuthResponseDTO.builder()
                        .user(user)
                        .accessToken(json.get("access_token").asText())
                        .refreshToken(json.get("refresh_token").asText())
                        .build();
            } else if (response.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                throw new AuthException("Credenciais inválidas");
            } else {
                throw new SupabaseIntegrationException("Erro no login no Supabase", null);
            }
        } catch (HttpClientErrorException.Unauthorized e) {
            throw new AuthException("Credenciais inválidas");
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            throw new SupabaseIntegrationException("Erro ao fazer login no Supabase", e);
        } catch (RestClientException e) {
            throw new SupabaseIntegrationException("Falha de conexão com o Supabase", e);
        } catch (Exception e) {
            throw new SupabaseIntegrationException("Erro inesperado no login", e);
        }
    }
}
