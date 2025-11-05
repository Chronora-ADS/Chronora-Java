package br.com.senai.service;

import br.com.senai.model.DTO.SupabaseAuthResponseDTO;
import br.com.senai.model.DTO.SupabaseUserDTO;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
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

            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    url, HttpMethod.GET, entity, String.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                JsonNode userData = objectMapper.readTree(response.getBody());

                return SupabaseUserDTO.builder()
                        .id(userData.get("id").asText())
                        .email(userData.get("email").asText())
                        .phone(userData.has("phone") ? userData.get("phone").asText() : null)
                        .createdAt(userData.get("created_at").asText())
                        .build();
            } else {
                throw new RuntimeException("Token inválido");
            }
        } catch (Exception e) {
            throw new RuntimeException("Erro ao validar token: " + e.getMessage());
        }
    }

    /**
     * Registra um usuário no Supabase
     */
    public SupabaseUserDTO signUp(String email, String password, Map<String, Object> userMetadata) {
        try {
            String url = supabaseUrl + SIGNUP_ENDPOINT;

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("email", email);
            requestBody.put("password", password);
            requestBody.put("data", userMetadata);

            HttpHeaders headers = new HttpHeaders();
            headers.set("apikey", supabaseAnonKey);
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    url, HttpMethod.POST, entity, String.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                JsonNode responseData = objectMapper.readTree(response.getBody());
                JsonNode userData = responseData.get("user");

                return SupabaseUserDTO.builder()
                        .id(userData.get("id").asText())
                        .email(userData.get("email").asText())
                        .phone(userData.get("phone").asText())
                        .createdAt(userData.get("created_at").asText())
                        .build();
            } else {
                throw new RuntimeException("Erro no cadastro: " + response.getBody());
            }
        } catch (Exception e) {
            throw new RuntimeException("Erro ao cadastrar usuário: " + e.getMessage());
        }
    }

    /**
     * Login no Supabase
     */
    public SupabaseAuthResponseDTO signIn(String email, String password) {
        try {
            String url = supabaseUrl + TOKEN_ENDPOINT;

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("email", email);
            requestBody.put("password", password);

            HttpHeaders headers = new HttpHeaders();
            headers.set("apikey", supabaseAnonKey);
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    url, HttpMethod.POST, entity, String.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                JsonNode responseData = objectMapper.readTree(response.getBody());

                JsonNode userData = responseData.get("user");
                String accessToken = responseData.get("access_token").asText();
                String refreshToken = responseData.get("refresh_token").asText();

                SupabaseUserDTO user = SupabaseUserDTO.builder()
                        .id(userData.get("id").asText())
                        .email(userData.get("email").asText())
                        .phone(userData.has("phone") ? userData.get("phone").asText() : null)
                        .createdAt(userData.get("created_at").asText())
                        .build();

                return SupabaseAuthResponseDTO.builder()
                        .user(user)
                        .accessToken(accessToken)
                        .refreshToken(refreshToken)
                        .build();
            } else {
                throw new RuntimeException("Credenciais inválidas");
            }
        } catch (Exception e) {
            throw new RuntimeException("Erro no login: " + e.getMessage());
        }
    }
}
