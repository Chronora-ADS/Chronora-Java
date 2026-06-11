package br.com.senai.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.com.senai.exception.Auth.AuthException;
import br.com.senai.model.DTO.user.SupabaseUserDTO;
import br.com.senai.exception.Validation.EmailAlreadyExistsException;
import br.com.senai.exception.Validation.PhoneNumberAlreadyExistsException;
import br.com.senai.model.DTO.user.DocumentDTO;
import br.com.senai.model.DTO.user.LoginDTO;
import br.com.senai.model.DTO.user.UserDTO;
import br.com.senai.model.entity.UserEntity;
import br.com.senai.repository.UserRepository;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import br.com.senai.service.auth.AuthService;
import br.com.senai.service.service.SupabaseStorageService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private SupabaseStorageService storageService;

    @InjectMocks
    private AuthService authService;

    @Test
    void deveRegistrarUsuarioComDocumentoESenhaCriptografada() {
        UserDTO dto = criarUserDTO();
        when(userRepository.existsByEmail(dto.getEmail())).thenReturn(false);
        when(userRepository.existsByPhoneNumber(dto.getPhoneNumber())).thenReturn(false);
        when(passwordEncoder.encode(dto.getPassword())).thenReturn("senha-criptografada");
        when(storageService.uploadBase64Image(eq("base64-documento"), eq("users"), isNull(), eq("png")))
                .thenReturn("https://storage/documento.png");
        when(userRepository.save(any(UserEntity.class))).thenAnswer(invocation -> {
            UserEntity user = invocation.getArgument(0);
            user.setId(10L);
            return user;
        });

        UserEntity cadastrado = authService.register(dto, "supabase-123");

        assertEquals(10L, cadastrado.getId());
        assertEquals("Ana Silva", cadastrado.getName());
        assertEquals("ana@chronora.com", cadastrado.getEmail());
        assertEquals("senha-criptografada", cadastrado.getPassword());
        assertEquals("supabase-123", cadastrado.getSupabaseUserId());
        assertEquals(List.of("USER"), cadastrado.getRoles());
        assertEquals(12, cadastrado.getTimeChronos());
        assertEquals("documento.png", cadastrado.getDocumentEntity().getName());
        assertEquals("png", cadastrado.getDocumentEntity().getType());
        assertEquals("https://storage/documento.png", cadastrado.getDocumentEntity().getUrl());
    }

    @Test
    void deveFalharCadastroQuandoEmailJaExiste() {
        UserDTO dto = criarUserDTO();
        when(userRepository.existsByEmail(dto.getEmail())).thenReturn(true);

        assertThrows(EmailAlreadyExistsException.class, () -> authService.register(dto, "supabase-123"));

        verify(storageService, never()).uploadBase64Image(any(), any(), any(), any());
        verify(userRepository, never()).save(any());
    }

    @Test
    void deveFalharCadastroQuandoTelefoneJaExiste() {
        UserDTO dto = criarUserDTO();
        when(userRepository.existsByEmail(dto.getEmail())).thenReturn(false);
        when(userRepository.existsByPhoneNumber(dto.getPhoneNumber())).thenReturn(true);

        assertThrows(PhoneNumberAlreadyExistsException.class, () -> authService.register(dto, "supabase-123"));

        verify(storageService, never()).uploadBase64Image(any(), any(), any(), any());
        verify(userRepository, never()).save(any());
    }

    @Test
    void deveAutenticarLoginComEmailESenhaComSucesso() {
        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setEmail("ana@chronora.com");
        loginDTO.setPassword("senha123");
        UserEntity user = criarUsuario();

        when(userRepository.existsByEmail("ana@chronora.com")).thenReturn(true);
        when(passwordEncoder.matches("senha123", "hash-senha")).thenReturn(true);

        UserEntity autenticado = authService.authenticate(loginDTO);

        assertSame(user, autenticado);
        verify(userRepository).existsByEmail("ana@chronora.com");
    }

    @Test
    void deveRetornarErroQuandoSenhaDoLoginForIncorreta() {
        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setEmail("ana@chronora.com");
        loginDTO.setPassword("senha-errada");
        UserEntity user = criarUsuario();

        when(userRepository.existsByEmail("ana@chronora.com")).thenReturn(true);
        when(passwordEncoder.matches("senha-errada", "hash-senha")).thenReturn(false);

        assertThrows(AuthException.class, () -> authService.authenticate(loginDTO));
    }

    @Test
    void deveRetornarErroQuandoEmailDoLoginNaoExistir() {
        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setEmail("inexistente@chronora.com");
        loginDTO.setPassword("senha123");
        when(userRepository.existsByEmail("inexistente@chronora.com")).thenReturn(false);

        assertThrows(AuthException.class, () -> authService.authenticate(loginDTO));

        verify(passwordEncoder, never()).matches(any(), any());
    }

    @Test
    void deveCarregarUsuarioSpringSecurityPeloEmail() {
        UserEntity user = criarUsuario();
        when(userRepository.existsByEmail("ana@chronora.com")).thenReturn(true);

        var details = authService.loadUserByUsername("ana@chronora.com");

        assertEquals("ana@chronora.com", details.getUsername());
        assertEquals("hash-senha", details.getPassword());
        assertTrue(details.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_USER")));
    }

    @Test
    void deveRetornarErroAoCarregarUsuarioSpringSecurityInexistente() {
        when(userRepository.existsByEmail("inexistente@chronora.com")).thenReturn(false);

        assertThrows(UsernameNotFoundException.class,
                () -> authService.loadUserByUsername("inexistente@chronora.com"));
    }

    @Test
    void deveConstruirMetadataComNomeETelefoneParaSupabase() {
        UserDTO dto = criarUserDTO();

        Map<String, Object> metadata = authService.buildUserMetadata(dto);

        assertEquals("Ana Silva", metadata.get("name"));
        assertEquals(11999999999L, metadata.get("phone"));
    }

    @Test
    void deveAtualizarSenhaLocalComHash() {
        UserEntity user = criarUsuario();
        when(userRepository.findBySupabaseUserId("supabase-123")).thenReturn(Optional.of(user));
        when(passwordEncoder.encode("novaSenha123")).thenReturn("novo-hash");

        authService.updatePassword("supabase-123", "novaSenha123");

        ArgumentCaptor<UserEntity> userCaptor = ArgumentCaptor.forClass(UserEntity.class);
        verify(userRepository).save(userCaptor.capture());
        assertEquals("novo-hash", userCaptor.getValue().getPassword());
    }

    @Test
    void deveResolverUsuarioPorEmailESincronizarSupabaseUserIdQuandoIdNaoEstiverVinculado() {
        UserEntity user = criarUsuario();
        user.setSupabaseUserId(null);
        SupabaseUserDTO supabaseUser = SupabaseUserDTO.builder()
                .id("supabase-novo")
                .email("ana@chronora.com")
                .build();

        when(userRepository.findBySupabaseUserId("supabase-novo")).thenReturn(Optional.empty());
        when(userRepository.existsByEmail("ana@chronora.com")).thenReturn(true);
        when(userRepository.save(any(UserEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserEntity resolved = authService.resolveUserForSupabaseUser(supabaseUser);

        assertSame(user, resolved);
        assertEquals("supabase-novo", resolved.getSupabaseUserId());
        verify(userRepository).save(user);
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
        UserEntity user = new UserEntity();
        user.setId(1L);
        user.setName("Ana Silva");
        user.setEmail("ana@chronora.com");
        user.setPhoneNumber(11999999999L);
        user.setPassword("hash-senha");
        user.setTimeChronos(40);
        user.setRoles(List.of("USER"));
        user.setSupabaseUserId("supabase-123");
        return user;
    }
}
