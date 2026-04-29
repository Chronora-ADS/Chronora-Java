package br.com.senai.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.com.senai.model.DTO.DocumentDTO;
import br.com.senai.model.DTO.LoginDTO;
import br.com.senai.model.DTO.SupabaseAuthResponseDTO;
import br.com.senai.model.DTO.SupabaseUserDTO;
import br.com.senai.model.DTO.UserDTO;
import br.com.senai.model.entity.DocumentEntity;
import br.com.senai.model.entity.UserEntity;
import br.com.senai.service.AuthService;
import br.com.senai.service.SupabaseAuthService;
import br.com.senai.util.JWTBlacklist;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthService authService;

    @Mock
    private SupabaseAuthService supabaseAuthService;

    @Mock
    private JWTBlacklist jwtBlacklist;

    @InjectMocks
    private AuthController authController;

    @Test
    void deveRealizarLoginComEmailESenhaERetornarSessao() {
        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setEmail("ana@chronora.com");
        loginDTO.setPassword("senha123");
        SupabaseAuthResponseDTO session = SupabaseAuthResponseDTO.builder()
                .accessToken("access-token")
                .refreshToken("refresh-token")
                .expiresIn(3600L)
                .build();
        when(supabaseAuthService.signIn("ana@chronora.com", "senha123")).thenReturn(session);

        ResponseEntity<Map<String, Object>> response = authController.login(loginDTO);

        assertEquals(200, response.getStatusCode().value());
        assertEquals("access-token", response.getBody().get("access_token"));
        assertEquals("refresh-token", response.getBody().get("refresh_token"));
        assertEquals(3600L, response.getBody().get("expires_in"));
        verify(supabaseAuthService).signIn("ana@chronora.com", "senha123");
    }

    @Test
    void deveRegistrarUsuarioComSucesso() {
        UserDTO userDTO = criarUserDTO();
        SupabaseUserDTO supabaseUser = SupabaseUserDTO.builder()
                .id("supabase-123")
                .email("ana@chronora.com")
                .build();
        UserEntity cadastrado = criarUsuario();

        when(authService.buildUserMetadata(userDTO)).thenReturn(Map.of("name", "Ana Silva", "phone", 11999999999L));
        when(supabaseAuthService.signUp(eq("ana@chronora.com"), eq("senha123"), anyMap()))
                .thenReturn(supabaseUser);
        when(authService.register(userDTO, "supabase-123")).thenReturn(cadastrado);

        var response = authController.register(userDTO);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(1L, response.getBody().getId());
        assertEquals("Ana Silva", response.getBody().getName());
        assertEquals("ana@chronora.com", response.getBody().getEmail());
        verify(authService).validateUniqueEmailAndPhone("ana@chronora.com", 11999999999L);
        verify(supabaseAuthService).signUp(eq("ana@chronora.com"), eq("senha123"), anyMap());
        verify(authService).register(userDTO, "supabase-123");
    }

    @Test
    void deveExcluirUsuarioSupabaseQuandoCadastroLocalFalhar() {
        UserDTO userDTO = criarUserDTO();
        SupabaseUserDTO supabaseUser = SupabaseUserDTO.builder()
                .id("supabase-123")
                .email("ana@chronora.com")
                .build();
        RuntimeException erroCadastroLocal = new RuntimeException("falha local");

        when(authService.buildUserMetadata(userDTO)).thenReturn(Map.of("name", "Ana Silva"));
        when(supabaseAuthService.signUp(eq("ana@chronora.com"), eq("senha123"), anyMap()))
                .thenReturn(supabaseUser);
        when(authService.register(userDTO, "supabase-123")).thenThrow(erroCadastroLocal);

        RuntimeException erro = assertThrows(RuntimeException.class, () -> authController.register(userDTO));

        assertSame(erroCadastroLocal, erro);
        verify(supabaseAuthService).deleteUser("supabase-123");
    }

    @Test
    void deveAdicionarTokenNaBlacklistAoFazerLogout() {
        var response = authController.logout("Bearer token-para-invalidar");

        assertEquals(200, response.getStatusCode().value());
        verify(jwtBlacklist).addToken("token-para-invalidar");
    }

    @Test
    void deveValidarTokenERetornarUsuarioLocal() {
        SupabaseUserDTO supabaseUser = SupabaseUserDTO.builder()
                .id("supabase-123")
                .email("ana@chronora.com")
                .build();
        UserEntity user = criarUsuario();
        when(supabaseAuthService.validateToken("access-token")).thenReturn(supabaseUser);
        when(authService.findBySupabaseUserId("supabase-123")).thenReturn(user);

        var response = authController.validateToken("Bearer access-token");

        assertEquals(200, response.getStatusCode().value());
        assertEquals("Ana Silva", response.getBody().getName());
        verify(supabaseAuthService).validateToken("access-token");
        verify(authService).findBySupabaseUserId("supabase-123");
    }

    @Test
    void deveRetornarErroQuandoTokenNaoForBearerNaValidacao() {
        assertThrows(IllegalArgumentException.class, () -> authController.validateToken("access-token"));
    }

    private UserDTO criarUserDTO() {
        DocumentDTO documentDTO = new DocumentDTO();
        documentDTO.setName("documento.png");
        documentDTO.setType("png");
        documentDTO.setData("base64-documento");

        UserDTO dto = new UserDTO();
        dto.setName("Ana Silva");
        dto.setEmail("ana@chronora.com");
        dto.setPhoneNumber(11999999999L);
        dto.setPassword("senha123");
        dto.setDocument(documentDTO);
        return dto;
    }

    private UserEntity criarUsuario() {
        DocumentEntity documentEntity = new DocumentEntity();
        documentEntity.setName("documento.png");
        documentEntity.setType("png");
        documentEntity.setUrl("https://storage/documento.png");

        UserEntity user = new UserEntity();
        user.setId(1L);
        user.setName("Ana Silva");
        user.setEmail("ana@chronora.com");
        user.setPhoneNumber(11999999999L);
        user.setPassword("hash");
        user.setTimeChronos(12);
        user.setRoles(List.of("USER"));
        user.setSupabaseUserId("supabase-123");
        user.setDocumentEntity(documentEntity);
        return user;
    }
}
