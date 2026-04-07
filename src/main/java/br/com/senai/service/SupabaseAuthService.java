package br.com.senai.service;

import br.com.senai.exception.Auth.AuthException;
import br.com.senai.exception.SupabaseIntegrationException;
import br.com.senai.exception.Validation.EmailAlreadyExistsException;
import br.com.senai.model.DTO.LoginDTO;
import br.com.senai.model.DTO.SupabaseAuthResponseDTO;
import br.com.senai.model.DTO.SupabaseUserDTO;
import br.com.senai.model.entity.UserEntity;
import br.com.senai.repository.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class SupabaseAuthService {

    private static final String USER_ENDPOINT = "/auth/v1/user";
    private static final String SIGNUP_ENDPOINT = "/auth/v1/signup";
    private static final String TOKEN_ENDPOINT = "/auth/v1/token?grant_type=password";
    private static final String LOCAL_TOKEN_PREFIX = "local-token:";

    @Value("${supabase.url}")
    private String supabaseUrl;

    @Value("${supabase.anon-key}")
    private String supabaseAnonKey;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final UserRepository userRepository;
    private final AuthService authService;

    public SupabaseAuthService(
            RestTemplate restTemplate,
            ObjectMapper objectMapper,
            UserRepository userRepository,
            AuthService authService
    ) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.userRepository = userRepository;
        this.authService = authService;
    }

    public SupabaseUserDTO validateToken(String token) {
        if (!isSupabaseConfigured()) {
            return validateLocalToken(token);
        }

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
            }
            if (response.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                throw new AuthException("Token invalido ou expirado");
            }
            throw new SupabaseIntegrationException("Resposta inesperada do Supabase: " + response.getStatusCode(), null);
        } catch (HttpClientErrorException.Unauthorized e) {
            throw new AuthException("Token invalido");
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            throw new SupabaseIntegrationException("Erro na chamada ao Supabase", e);
        } catch (RestClientException e) {
            throw new SupabaseIntegrationException("Falha de conexao com o Supabase", e);
        } catch (Exception e) {
            throw new SupabaseIntegrationException("Erro inesperado ao validar token", e);
        }
    }

    public SupabaseUserDTO signUp(String email, String password, Map<String, Object> userMetadata) {
        if (!isSupabaseConfigured()) {
            if (userRepository.findByEmail(email).isPresent()) {
                throw new EmailAlreadyExistsException(email);
            }

            Object phone = userMetadata.get("phone");
            return SupabaseUserDTO.builder()
                    .id("local-user-" + UUID.randomUUID())
                    .email(email)
                    .phone(phone == null ? null : phone.toString())
                    .createdAt(OffsetDateTime.now().toString())
                    .build();
        }

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
            }
            if (response.getStatusCode() == HttpStatus.CONFLICT) {
                throw new EmailAlreadyExistsException(email);
            }
            throw new SupabaseIntegrationException("Erro no cadastro no Supabase: " + response.getStatusCode(), null);
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

    public SupabaseAuthResponseDTO signIn(String email, String password) {
        if (!isSupabaseConfigured()) {
            LoginDTO loginDTO = new LoginDTO();
            loginDTO.setEmail(email);
            loginDTO.setPassword(password);

            UserEntity user = authService.authenticate(loginDTO);
            String localUserId = ensureLocalUserId(user);

            return SupabaseAuthResponseDTO.builder()
                    .user(buildLocalUser(user))
                    .accessToken(LOCAL_TOKEN_PREFIX + localUserId)
                    .refreshToken(LOCAL_TOKEN_PREFIX + localUserId)
                    .build();
        }

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
            }
            if (response.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                throw new AuthException("Credenciais invalidas");
            }
            throw new SupabaseIntegrationException("Erro no login no Supabase", null);
        } catch (HttpClientErrorException.Unauthorized e) {
            throw new AuthException("Credenciais invalidas");
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            throw new SupabaseIntegrationException("Erro ao fazer login no Supabase", e);
        } catch (RestClientException e) {
            throw new SupabaseIntegrationException("Falha de conexao com o Supabase", e);
        } catch (Exception e) {
            throw new SupabaseIntegrationException("Erro inesperado no login", e);
        }
    }

    private boolean isSupabaseConfigured() {
        return StringUtils.hasText(supabaseUrl) && StringUtils.hasText(supabaseAnonKey);
    }

    private SupabaseUserDTO validateLocalToken(String token) {
        if (!StringUtils.hasText(token) || !token.startsWith(LOCAL_TOKEN_PREFIX)) {
            throw new AuthException("Token invalido");
        }

        String supabaseUserId = token.substring(LOCAL_TOKEN_PREFIX.length());
        UserEntity user = authService.findBySupabaseUserId(supabaseUserId);
        return buildLocalUser(user);
    }

    private SupabaseUserDTO buildLocalUser(UserEntity user) {
        return SupabaseUserDTO.builder()
                .id(ensureLocalUserId(user))
                .email(user.getEmail())
                .phone(user.getPhoneNumber() == null ? null : user.getPhoneNumber().toString())
                .createdAt(OffsetDateTime.now().toString())
                .build();
    }

    private String ensureLocalUserId(UserEntity user) {
        if (StringUtils.hasText(user.getSupabaseUserId())) {
            return user.getSupabaseUserId();
        }

        String generatedId = "local-user-" + UUID.randomUUID();
        user.setSupabaseUserId(generatedId);
        userRepository.save(user);
        return generatedId;
    }
}
