package br.com.senai.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import br.com.senai.exception.Auth.AuthException;
import br.com.senai.exception.NotFound.UserNotFoundException;
import br.com.senai.exception.Validation.EmailAlreadyExistsException;
import br.com.senai.exception.Validation.PhoneNumberAlreadyExistsException;
import br.com.senai.exception.Validation.QuantityChronosInvalidException;
import br.com.senai.model.DTO.user.DocumentDTO;
import br.com.senai.model.DTO.user.SupabaseUserDTO;
import br.com.senai.model.DTO.user.UserEditDTO;
import br.com.senai.model.entity.UserEntity;
import br.com.senai.repository.NotificationRepository;
import br.com.senai.repository.ServiceRepository;
import br.com.senai.repository.UserRepository;
import java.util.Optional;

import br.com.senai.service.auth.AuthService;
import br.com.senai.service.auth.SupabaseAuthService;
import br.com.senai.service.service.SupabaseStorageService;
import br.com.senai.service.user.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    private static final String TOKEN_HEADER = "Bearer token-valido";

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuthService authService;

    @Mock
    private SupabaseAuthService supabaseAuthService;

    @Mock
    private SupabaseStorageService storageService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private ServiceRepository serviceRepository;

    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private UserService userService;

    private UserEntity usuarioLogado;

    @BeforeEach
    void setUp() {
        usuarioLogado = criarUsuario(1L, "Ana Silva", "ana@chronora.com", 100);
    }

    @Test
    void deveComprarChronosComSucessoSemExcederLimiteDaCarteira() {
        prepararUsuarioLogado(usuarioLogado);
        when(userRepository.save(usuarioLogado)).thenReturn(usuarioLogado);

        UserEntity atualizado = userService.buyChronos(TOKEN_HEADER, 50);

        assertEquals(150, atualizado.getTimeChronos());
        verify(userRepository).save(usuarioLogado);
    }

    @Test
    void deveRetornarErroQuandoCompraExcederLimiteDeTrezentosChronos() {
        usuarioLogado.setTimeChronos(290);
        prepararUsuarioLogado(usuarioLogado);

        assertThrows(QuantityChronosInvalidException.class, () -> userService.buyChronos(TOKEN_HEADER, 20));

        verify(userRepository, never()).save(any());
    }

    @Test
    void deveRetornarErroQuandoCompraTiverQuantidadeInvalida() {
        prepararUsuarioLogado(usuarioLogado);

        assertThrows(QuantityChronosInvalidException.class, () -> userService.buyChronos(TOKEN_HEADER, 0));

        verify(userRepository, never()).save(any());
    }

    @Test
    void deveVenderChronosComSucesso() {
        prepararUsuarioLogado(usuarioLogado);
        when(userRepository.save(usuarioLogado)).thenReturn(usuarioLogado);

        UserEntity atualizado = userService.sellChronos(TOKEN_HEADER, 30);

        assertEquals(70, atualizado.getTimeChronos());
        verify(userRepository).save(usuarioLogado);
    }

    @Test
    void deveRetornarErroQuandoVendaDeixarSaldoNegativo() {
        usuarioLogado.setTimeChronos(10);
        prepararUsuarioLogado(usuarioLogado);

        assertThrows(QuantityChronosInvalidException.class, () -> userService.sellChronos(TOKEN_HEADER, 11));

        verify(userRepository, never()).save(any());
    }

    @Test
    void deveRetornarErroQuandoTokenNaoForBearer() {
        assertThrows(AuthException.class, () -> userService.getLoggedUser("token-sem-prefixo"));

        verify(supabaseAuthService, never()).validateToken(any());
        verify(authService, never()).resolveUserForSupabaseUser(any());
    }

    @Test
    void deveRetornarErroQuandoUsuarioDoTokenNaoExistir() {
        when(supabaseAuthService.validateToken("token-valido")).thenReturn(criarSupabaseUserDTO("supabase-123"));
        when(authService.resolveUserForSupabaseUser(any(SupabaseUserDTO.class)))
                .thenThrow(new UserNotFoundException("Usuario nao encontrado."));

        assertThrows(UserNotFoundException.class, () -> userService.getLoggedUser(TOKEN_HEADER));
    }

    @Test
    void deveEditarPerfilComSucessoQuandoUsuarioForDonoDaConta() {
        prepararUsuarioLogado(usuarioLogado);
        UserEditDTO editDTO = new UserEditDTO();
        editDTO.setId(1L);
        editDTO.setName(" Ana Atualizada ");
        editDTO.setEmail("ana.atualizada@chronora.com");
        editDTO.setPhoneNumber(11888888888L);
        editDTO.setPassword("novaSenha123");
        editDTO.setDocument(criarDocumentoDTO());
        editDTO.setProfileImage(criarImagemPerfilDTO());

        when(userRepository.existsByEmail("ana.atualizada@chronora.com")).thenReturn(false);
        when(userRepository.existsByPhoneNumber(11888888888L)).thenReturn(false);
        when(passwordEncoder.encode("novaSenha123")).thenReturn("hash-novo");
        when(storageService.uploadBase64Image(eq("base64-documento"), eq("users"), eq("token-valido"), eq("png")))
                .thenReturn("https://storage/novo-documento.png");
        when(storageService.uploadBase64Image(eq("base64-avatar"), eq("users"), eq("token-valido"), eq("jpg")))
                .thenReturn("https://storage/avatar.jpg");
        when(userRepository.save(usuarioLogado)).thenReturn(usuarioLogado);

        UserEntity atualizado = userService.put(editDTO, TOKEN_HEADER);

        assertSame(usuarioLogado, atualizado);
        assertEquals("Ana Atualizada", atualizado.getName());
        assertEquals("ana.atualizada@chronora.com", atualizado.getEmail());
        assertEquals(11888888888L, atualizado.getPhoneNumber());
        assertEquals("hash-novo", atualizado.getPassword());
        assertEquals("https://storage/novo-documento.png", atualizado.getDocumentEntity().getUrl());
        assertEquals("https://storage/avatar.jpg", atualizado.getProfileImage());
        verify(supabaseAuthService).updateUser(
                eq("token-valido"),
                eq("ana.atualizada@chronora.com"),
                eq("novaSenha123"),
                anyMap()
        );
    }

    @Test
    void deveRetornarErroQuandoUsuarioTentarEditarOutraConta() {
        prepararUsuarioLogado(usuarioLogado);
        UserEditDTO editDTO = new UserEditDTO();
        editDTO.setId(99L);
        editDTO.setName("Outro nome");

        assertThrows(AuthException.class, () -> userService.put(editDTO, TOKEN_HEADER));

        verify(userRepository, never()).save(any());
        verify(supabaseAuthService, never()).updateUser(any(), any(), any(), any());
    }

    @Test
    void deveRetornarErroQuandoEmailEditadoJaPertencerAOutroUsuario() {
        prepararUsuarioLogado(usuarioLogado);
        UserEditDTO editDTO = new UserEditDTO();
        editDTO.setId(1L);
        editDTO.setEmail("duplicado@chronora.com");
        when(userRepository.existsByEmail("duplicado@chronora.com")).thenReturn(true);
        assertThrows(EmailAlreadyExistsException.class, () -> userService.put(editDTO, TOKEN_HEADER));
        verify(userRepository, never()).save(any());
    }

    @Test
    void deveRetornarErroQuandoTelefoneEditadoJaPertencerAOutroUsuario() {
        prepararUsuarioLogado(usuarioLogado);
        UserEditDTO editDTO = new UserEditDTO();
        editDTO.setId(1L);
        editDTO.setPhoneNumber(11777777777L);
        UserEntity outroUsuario = criarUsuario(2L, "Bia", "bia@chronora.com", 20);
        outroUsuario.setPhoneNumber(11777777777L);
        when(userRepository.existsByPhoneNumber(11777777777L)).thenReturn(true);
        assertThrows(PhoneNumberAlreadyExistsException.class, () -> userService.put(editDTO, TOKEN_HEADER));
        verify(userRepository, never()).save(any());
    }

    @Test
    void deveDesativarUsuarioSemExcluirDadosRelacionados() {
        prepararUsuarioLogado(usuarioLogado);
        usuarioLogado.setSupabaseUserId("supabase-123");

        userService.delete(TOKEN_HEADER);

        assertFalse(usuarioLogado.isActive());
        assertNotNull(usuarioLogado.getDeletedAt());
        verify(userRepository).save(usuarioLogado);
        verify(userRepository, never()).delete(any(UserEntity.class));
        verify(supabaseAuthService, never()).deleteUser(any());
        verifyNoInteractions(serviceRepository, notificationRepository);
    }

    private void prepararUsuarioLogado(UserEntity user) {
        when(supabaseAuthService.validateToken("token-valido")).thenReturn(criarSupabaseUserDTO(user.getSupabaseUserId()));
        when(authService.resolveUserForSupabaseUser(any(SupabaseUserDTO.class))).thenReturn(user);
    }

    private SupabaseUserDTO criarSupabaseUserDTO(String id) {
        return SupabaseUserDTO.builder()
                .id(id)
                .email("ana@chronora.com")
                .phone("11999999999")
                .createdAt("2026-04-27T00:00:00Z")
                .build();
    }

    private UserEntity criarUsuario(Long id, String nome, String email, int chronos) {
        UserEntity user = new UserEntity();
        user.setId(id);
        user.setName(nome);
        user.setEmail(email);
        user.setPhoneNumber(11999999999L + id);
        user.setPassword("hash-senha");
        user.setTimeChronos(chronos);
        user.setSupabaseUserId("supabase-" + id);
        return user;
    }

    private DocumentDTO criarDocumentoDTO() {
        DocumentDTO documentDTO = new DocumentDTO();
        documentDTO.setName("novo-documento.png");
        documentDTO.setType("png");
        documentDTO.setData("base64-documento");
        return documentDTO;
    }

    private DocumentDTO criarImagemPerfilDTO() {
        DocumentDTO imageDTO = new DocumentDTO();
        imageDTO.setName("avatar.jpg");
        imageDTO.setType("jpg");
        imageDTO.setData("base64-avatar");
        return imageDTO;
    }

}
