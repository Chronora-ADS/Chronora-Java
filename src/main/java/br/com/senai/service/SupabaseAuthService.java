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
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
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

@Service
public class SupabaseAuthService {

    private static final String USER_ENDPOINT = "/auth/v1/user";
    private static final String SIGNUP_ENDPOINT = "/auth/v1/signup";
    private static final String PASSWORD_TOKEN_ENDPOINT = "/auth/v1/token?grant_type=password";
    private static final String REFRESH_TOKEN_ENDPOINT = "/auth/v1/token?grant_type=refresh_token";
    private static final String RECOVERY_ENDPOINT = "/auth/v1/recover";
    private static final String ADMIN_USERS_ENDPOINT = "/auth/v1/admin/users/";
    private static final String LOCAL_TOKEN_PREFIX = "local-token:";
    private static final long LOCAL_TOKEN_TTL_SECONDS = 3600L;

    @Value("${supabase.url}")
    private String supabaseUrl;

    @Value("${supabase.anon-key}")
    private String supabaseAnonKey;

    @Value("${supabase.service-role:}")
    private String supabaseServiceRole;

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
            ResponseEntity<String> response = restTemplate.exchange(
                    supabaseUrl + USER_ENDPOINT,
                    HttpMethod.GET,
                    new HttpEntity<>(buildBearerHeaders(token, supabaseAnonKey)),
                    String.class
            );

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
            Map<String, Object> body = new HashMap<>();
            body.put("email", email);
            body.put("password", password);
            body.put("data", userMetadata);

            ResponseEntity<String> response = restTemplate.exchange(
                    supabaseUrl + SIGNUP_ENDPOINT,
                    HttpMethod.POST,
                    new HttpEntity<>(body, buildJsonHeaders(supabaseAnonKey)),
                    String.class
            );

            if (response.getStatusCode() == HttpStatus.OK || response.getStatusCode() == HttpStatus.CREATED) {
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
                    .expiresIn(LOCAL_TOKEN_TTL_SECONDS)
                    .build();
        }

        try {
            Map<String, Object> body = new HashMap<>();
            body.put("email", email);
            body.put("password", password);

            ResponseEntity<String> response = restTemplate.exchange(
                    supabaseUrl + PASSWORD_TOKEN_ENDPOINT,
                    HttpMethod.POST,
                    new HttpEntity<>(body, buildJsonHeaders(supabaseAnonKey)),
                    String.class
            );

            if (response.getStatusCode() == HttpStatus.OK) {
                return parseAuthResponse(response.getBody());
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

    public SupabaseAuthResponseDTO refreshSession(String refreshToken) {
        if (!StringUtils.hasText(refreshToken)) {
            throw new AuthException("Refresh token obrigatorio");
        }

        if (!isSupabaseConfigured()) {
            SupabaseUserDTO user = validateLocalToken(refreshToken);
            return SupabaseAuthResponseDTO.builder()
                    .user(user)
                    .accessToken(refreshToken)
                    .refreshToken(refreshToken)
                    .expiresIn(LOCAL_TOKEN_TTL_SECONDS)
                    .build();
        }

        try {
            Map<String, Object> body = new HashMap<>();
            body.put("refresh_token", refreshToken);

            ResponseEntity<String> response = restTemplate.exchange(
                    supabaseUrl + REFRESH_TOKEN_ENDPOINT,
                    HttpMethod.POST,
                    new HttpEntity<>(body, buildJsonHeaders(supabaseAnonKey)),
                    String.class
            );

            if (response.getStatusCode() == HttpStatus.OK) {
                return parseAuthResponse(response.getBody());
            }
            if (response.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                throw new AuthException("Refresh token invalido");
            }
            throw new SupabaseIntegrationException("Erro ao renovar sessao no Supabase", null);
        } catch (HttpClientErrorException.Unauthorized e) {
            throw new AuthException("Refresh token invalido");
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            throw new SupabaseIntegrationException("Erro ao renovar sessao no Supabase", e);
        } catch (RestClientException e) {
            throw new SupabaseIntegrationException("Falha de conexao com o Supabase", e);
        } catch (Exception e) {
            throw new SupabaseIntegrationException("Erro inesperado ao renovar sessao", e);
        }
    }

    public void sendPasswordRecovery(String email) {
        sendPasswordRecovery(email, null);
    }

    public void sendPasswordRecovery(String email, String redirectTo) {
        if (!StringUtils.hasText(email)) {
            throw new AuthException("Email obrigatorio");
        }

        if (!isSupabaseConfigured()) {
            return;
        }

        try {
            Map<String, Object> body = new HashMap<>();
            body.put("email", email);
            if (StringUtils.hasText(redirectTo)) {
                body.put("redirect_to", redirectTo);
            }

            restTemplate.exchange(
                    supabaseUrl + RECOVERY_ENDPOINT,
                    HttpMethod.POST,
                    new HttpEntity<>(body, buildJsonHeaders(supabaseAnonKey)),
                    String.class
            );
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            throw new SupabaseIntegrationException("Erro ao solicitar recuperacao de senha", e);
        } catch (RestClientException e) {
            throw new SupabaseIntegrationException("Falha de conexao com o Supabase", e);
        }
    }

    public void resetPassword(String accessToken, String newPassword) {
        if (!StringUtils.hasText(newPassword)) {
            throw new AuthException("Nova senha obrigatoria");
        }

        SupabaseUserDTO user = validateToken(accessToken);

        if (isSupabaseConfigured()) {
            try {
                Map<String, Object> body = new HashMap<>();
                body.put("password", newPassword);

                restTemplate.exchange(
                        supabaseUrl + USER_ENDPOINT,
                        HttpMethod.PUT,
                        new HttpEntity<>(body, buildBearerHeaders(accessToken, supabaseAnonKey)),
                        String.class
                );
            } catch (HttpClientErrorException | HttpServerErrorException e) {
                throw new SupabaseIntegrationException("Erro ao redefinir senha no Supabase", e);
            } catch (RestClientException e) {
                throw new SupabaseIntegrationException("Falha de conexao com o Supabase", e);
            }
        }

        authService.updatePassword(user.getId(), newPassword);
    }

    public void deleteUser(String supabaseUserId) {
        if (!isSupabaseConfigured() || !StringUtils.hasText(supabaseServiceRole) || !StringUtils.hasText(supabaseUserId)) {
            return;
        }

        try {
            String url = supabaseUrl + ADMIN_USERS_ENDPOINT + supabaseUserId;
            HttpHeaders headers = new HttpHeaders();
            headers.set("apikey", supabaseServiceRole);
            headers.setBearerAuth(supabaseServiceRole);

            restTemplate.exchange(url, HttpMethod.DELETE, new HttpEntity<>(headers), String.class);
        } catch (RestClientException ignored) {
            // rollback compensatorio best-effort para nao esconder erro principal do cadastro
        }
    }

    private boolean isSupabaseConfigured() {
        return StringUtils.hasText(supabaseUrl) && StringUtils.hasText(supabaseAnonKey);
    }

    private SupabaseAuthResponseDTO parseAuthResponse(String body) throws Exception {
        JsonNode json = objectMapper.readTree(body);
        JsonNode userNode = json.get("user");

        SupabaseUserDTO user = SupabaseUserDTO.builder()
                .id(userNode.get("id").asText())
                .email(userNode.get("email").asText())
                .phone(userNode.has("phone") ? userNode.get("phone").asText() : null)
                .createdAt(userNode.get("created_at").asText())
                .build();

        Long expiresIn = json.has("expires_in") && !json.get("expires_in").isNull()
                ? json.get("expires_in").asLong()
                : null;

        return SupabaseAuthResponseDTO.builder()
                .user(user)
                .accessToken(json.has("access_token") ? json.get("access_token").asText() : null)
                .refreshToken(json.has("refresh_token") ? json.get("refresh_token").asText() : null)
                .expiresIn(expiresIn)
                .build();
    }

    private HttpHeaders buildJsonHeaders(String apiKey) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("apikey", apiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    private HttpHeaders buildBearerHeaders(String token, String apiKey) {
        HttpHeaders headers = buildJsonHeaders(apiKey);
        headers.setBearerAuth(token);
        return headers;
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
