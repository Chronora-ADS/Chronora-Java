package br.com.senai.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.com.senai.exception.Auth.AuthException;
import br.com.senai.exception.NotFound.UserNotFoundException;
import br.com.senai.exception.Validation.EmailAlreadyExistsException;
import br.com.senai.exception.Validation.PhoneNumberAlreadyExistsException;
import br.com.senai.exception.Validation.QuantityChronosInvalidException;
import br.com.senai.model.DTO.DocumentDTO;
import br.com.senai.model.DTO.SupabaseUserDTO;
import br.com.senai.model.DTO.UserEditDTO;
import br.com.senai.model.entity.ServiceEntity;
import br.com.senai.model.entity.UserEntity;
import br.com.senai.model.enums.ServiceStatus;
import br.com.senai.repository.NotificationRepository;
import br.com.senai.repository.ServiceRepository;
import br.com.senai.repository.UserRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
        verify(userRepository, never()).findBySupabaseUserId(any());
    }

    @Test
    void deveRetornarErroQuandoUsuarioDoTokenNaoExistir() {
        when(supabaseAuthService.validateToken("token-valido")).thenReturn(criarSupabaseUserDTO("supabase-123"));
        when(userRepository.findBySupabaseUserId("supabase-123")).thenReturn(Optional.empty());

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

        when(userRepository.findByEmail("ana.atualizada@chronora.com")).thenReturn(Optional.empty());
        when(userRepository.findByPhoneNumber(11888888888L)).thenReturn(Optional.empty());
        when(passwordEncoder.encode("novaSenha123")).thenReturn("hash-novo");
        when(storageService.uploadBase64Image(eq("base64-documento"), eq("users"), eq("token-valido"), eq("png")))
                .thenReturn("https://storage/novo-documento.png");
        when(userRepository.save(usuarioLogado)).thenReturn(usuarioLogado);

        UserEntity atualizado = userService.put(editDTO, TOKEN_HEADER);

        assertSame(usuarioLogado, atualizado);
        assertEquals("Ana Atualizada", atualizado.getName());
        assertEquals("ana.atualizada@chronora.com", atualizado.getEmail());
        assertEquals(11888888888L, atualizado.getPhoneNumber());
        assertEquals("hash-novo", atualizado.getPassword());
        assertEquals("https://storage/novo-documento.png", atualizado.getDocumentEntity().getUrl());
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
        when(userRepository.findByEmail("duplicado@chronora.com"))
                .thenReturn(Optional.of(criarUsuario(2L, "Bia", "duplicado@chronora.com", 20)));

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
        when(userRepository.findByPhoneNumber(11777777777L)).thenReturn(Optional.of(outroUsuario));

        assertThrows(PhoneNumberAlreadyExistsException.class, () -> userService.put(editDTO, TOKEN_HEADER));

        verify(userRepository, never()).save(any());
    }

    @Test
    void deveExcluirUsuarioERemoverDependencias() {
        prepararUsuarioLogado(usuarioLogado);
        usuarioLogado.setSupabaseUserId("supabase-123");
        ServiceEntity aceito = criarServico(10L, ServiceStatus.ACEITO, criarUsuario(2L, "Bia", "bia@chronora.com", 50));
        aceito.setUserAccepted(usuarioLogado);
        aceito.setVerificationCode("1234");
        ServiceEntity emAndamento = criarServico(11L, ServiceStatus.EM_ANDAMENTO, criarUsuario(3L, "Caio", "caio@chronora.com", 50));
        emAndamento.setUserAccepted(usuarioLogado);
        emAndamento.setVerificationCode("9999");
        ServiceEntity criadoPeloUsuario = criarServico(12L, ServiceStatus.CRIADO, usuarioLogado);

        when(serviceRepository.findAllByUserAccepted(usuarioLogado)).thenReturn(List.of(aceito, emAndamento));
        when(serviceRepository.findAllByUserCreator(usuarioLogado)).thenReturn(List.of(criadoPeloUsuario));

        userService.delete(TOKEN_HEADER);

        assertEquals(ServiceStatus.CRIADO, aceito.getStatus());
        assertNull(aceito.getUserAccepted());
        assertNull(aceito.getVerificationCode());
        assertEquals(ServiceStatus.CANCELADO, emAndamento.getStatus());
        assertNull(emAndamento.getUserAccepted());
        verify(notificationRepository).deleteAllByUser(usuarioLogado);
        verify(notificationRepository).deleteAllByServiceIn(List.of(criadoPeloUsuario));
        verify(serviceRepository).deleteAll(List.of(criadoPeloUsuario));
        verify(supabaseAuthService).deleteUser("supabase-123");
        verify(userRepository).delete(usuarioLogado);
    }

    private void prepararUsuarioLogado(UserEntity user) {
        when(supabaseAuthService.validateToken("token-valido")).thenReturn(criarSupabaseUserDTO(user.getSupabaseUserId()));
        when(userRepository.findBySupabaseUserId(user.getSupabaseUserId())).thenReturn(Optional.of(user));
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

    private ServiceEntity criarServico(Long id, ServiceStatus status, UserEntity criador) {
        ServiceEntity service = new ServiceEntity();
        service.setId(id);
        service.setTitle("Pedido " + id);
        service.setDescription("Descricao do pedido");
        service.setTimeChronos(10);
        service.setStatus(status);
        service.setUserCreator(criador);
        return service;
    }
}
