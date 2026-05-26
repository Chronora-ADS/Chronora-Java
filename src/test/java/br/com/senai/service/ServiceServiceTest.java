package br.com.senai.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.com.senai.exception.Auth.AuthException;
import br.com.senai.exception.Validation.ExpiredValidationCodeException;
import br.com.senai.exception.Validation.IncorrectValidationCodeException;
import br.com.senai.exception.Validation.QuantityChronosInvalidException;
import br.com.senai.model.DTO.ServiceDTO;
import br.com.senai.model.DTO.ServiceEditDTO;
import br.com.senai.model.entity.ServiceEntity;
import br.com.senai.model.entity.UserEntity;
import br.com.senai.model.enums.ServiceModality;
import br.com.senai.model.enums.ServiceStatus;
import br.com.senai.repository.ServiceRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ServiceServiceTest {

    private static final String TOKEN_HEADER = "Bearer token-valido";

    @Mock
    private ServiceRepository serviceRepository;

    @Mock
    private UserService userService;

    @Mock
    private SupabaseStorageService storageService;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private ServiceService serviceService;

    private UserEntity criador;
    private UserEntity prestador;

    @BeforeEach
    void setUp() {
        criador = criarUsuario(1L, "Ana", 80);
        prestador = criarUsuario(2L, "Bruno", 30);
    }

    @Test
    void deveCriarPedidoComSucessoEDebitarSaldoDoSolicitante() {
        ServiceDTO dto = criarServiceDTO(20);
        when(userService.getLoggedUser(TOKEN_HEADER)).thenReturn(criador);
        when(storageService.uploadBase64Image(eq("base64-imagem"), eq("services"), isNull()))
                .thenReturn("https://storage/servico.png");
        when(serviceRepository.save(any(ServiceEntity.class))).thenAnswer(invocation -> {
            ServiceEntity service = invocation.getArgument(0);
            service.setId(100L);
            return service;
        });

        ServiceEntity criado = serviceService.create(dto, TOKEN_HEADER);

        assertEquals(100L, criado.getId());
        assertEquals("Aula de Java", criado.getTitle());
        assertEquals(20, criado.getTimeChronos());
        assertEquals(ServiceStatus.CRIADO, criado.getStatus());
        assertEquals(ServiceModality.REMOTO, criado.getModality());
        assertSame(criador, criado.getUserCreator());
        assertEquals("https://storage/servico.png", criado.getServiceImageUrl());
        assertEquals(List.of("Programacao", "Backend"), criado.getCategories());
        assertNotNull(criado.getPostedAt());
        verify(userService).sellChronos(TOKEN_HEADER, 20);
        verify(notificationService).create("Pedido criado", criador, criado);
    }

    @Test
    void deveRetornarErroQuandoSaldoForInsuficienteParaCriarPedido() {
        ServiceDTO dto = criarServiceDTO(90);
        criador.setTimeChronos(80);
        when(userService.getLoggedUser(TOKEN_HEADER)).thenReturn(criador);

        assertThrows(QuantityChronosInvalidException.class, () -> serviceService.create(dto, TOKEN_HEADER));

        verify(userService, never()).sellChronos(any(), any());
        verify(serviceRepository, never()).save(any());
        verify(notificationService, never()).create(any(), any(), any());
    }

    @Test
    void deveRetornarErroQuandoPedidoTiverMenosDeUmChronos() {
        ServiceDTO dto = criarServiceDTO(0);
        when(userService.getLoggedUser(TOKEN_HEADER)).thenReturn(criador);

        assertThrows(QuantityChronosInvalidException.class, () -> serviceService.create(dto, TOKEN_HEADER));

        verify(serviceRepository, never()).save(any());
    }

    @Test
    void deveRetornarErroQuandoPedidoExcederCemChronos() {
        ServiceDTO dto = criarServiceDTO(101);
        when(userService.getLoggedUser(TOKEN_HEADER)).thenReturn(criador);

        assertThrows(QuantityChronosInvalidException.class, () -> serviceService.create(dto, TOKEN_HEADER));

        verify(serviceRepository, never()).save(any());
    }

    @Test
    void deveRetornarErroQuandoDescricaoForObrigatoria() {
        ServiceDTO dto = criarServiceDTO(10);
        dto.setDescription(" ");
        when(userService.getLoggedUser(TOKEN_HEADER)).thenReturn(criador);

        assertThrows(IllegalArgumentException.class, () -> serviceService.create(dto, TOKEN_HEADER));

        verify(serviceRepository, never()).save(any());
    }

    @Test
    void deveRetornarErroQuandoCategoriaForObrigatoria() {
        ServiceDTO dto = criarServiceDTO(10);
        dto.setCategories(List.of());
        when(userService.getLoggedUser(TOKEN_HEADER)).thenReturn(criador);

        assertThrows(IllegalArgumentException.class, () -> serviceService.create(dto, TOKEN_HEADER));

        verify(serviceRepository, never()).save(any());
    }

    @Test
    void deveEditarPedidoComSucessoQuandoUsuarioForProprietarioEAumentarChronos() {
        ServiceEntity service = criarServico(10L, criador, null, ServiceStatus.CRIADO, 20);
        ServiceEditDTO editDTO = new ServiceEditDTO();
        editDTO.setId(10L);
        editDTO.setTitle("Aula de Spring Boot");
        editDTO.setDescription("Mentoria focada em testes");
        editDTO.setTimeChronos(35);
        editDTO.setDeadline(LocalDate.now().plusDays(20));
        editDTO.setModality("Presencial");
        editDTO.setCategories(List.of("Java", "Testes"));

        when(userService.getLoggedUser(TOKEN_HEADER)).thenReturn(criador);
        when(serviceRepository.findById(10L)).thenReturn(Optional.of(service));
        when(serviceRepository.save(service)).thenReturn(service);

        ServiceEntity atualizado = serviceService.put(editDTO, TOKEN_HEADER);

        assertSame(service, atualizado);
        assertEquals("Aula de Spring Boot", atualizado.getTitle());
        assertEquals("Mentoria focada em testes", atualizado.getDescription());
        assertEquals(35, atualizado.getTimeChronos());
        assertEquals(ServiceModality.PRESENCIAL, atualizado.getModality());
        assertEquals(List.of("Java", "Testes"), atualizado.getCategories());
        verify(userService).sellChronos(TOKEN_HEADER, 15);
        verify(notificationService).create("Pedido editado", criador, service);
    }

    @Test
    void deveAtualizarCategoriasAPartirDoCampoCategoriesNaEdicao() {
        ServiceEntity service = criarServico(10L, criador, null, ServiceStatus.CRIADO, 20);
        ServiceEditDTO editDTO = new ServiceEditDTO();
        editDTO.setId(10L);
        editDTO.setCategories(List.of(" Categoria nova A ", "Categoria nova B"));

        when(userService.getLoggedUser(TOKEN_HEADER)).thenReturn(criador);
        when(serviceRepository.findById(10L)).thenReturn(Optional.of(service));
        when(serviceRepository.save(any(ServiceEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ServiceEntity atualizado = serviceService.put(editDTO, TOKEN_HEADER);

        assertEquals(List.of("Categoria nova A", "Categoria nova B"), atualizado.getCategories());
        ArgumentCaptor<ServiceEntity> serviceCaptor = ArgumentCaptor.forClass(ServiceEntity.class);
        verify(serviceRepository).save(serviceCaptor.capture());
        assertEquals(List.of("Categoria nova A", "Categoria nova B"), serviceCaptor.getValue().getCategories());
        verify(notificationService).create("Pedido editado", criador, service);
    }

    @Test
    void deveRejeitarCategoriaEmBrancoNaEdicao() {
        ServiceEntity service = criarServico(10L, criador, null, ServiceStatus.CRIADO, 20);
        ServiceEditDTO editDTO = new ServiceEditDTO();
        editDTO.setId(10L);
        editDTO.setCategories(List.of("Categoria valida", " "));

        when(userService.getLoggedUser(TOKEN_HEADER)).thenReturn(criador);
        when(serviceRepository.findById(10L)).thenReturn(Optional.of(service));

        assertThrows(IllegalArgumentException.class, () -> serviceService.put(editDTO, TOKEN_HEADER));

        verify(serviceRepository, never()).save(any());
        verify(notificationService, never()).create(any(), any(), any());
    }

    @Test
    void deveEditarPedidoComSucessoQuandoUsuarioForProprietarioEDevolverChronos() {
        ServiceEntity service = criarServico(10L, criador, null, ServiceStatus.CRIADO, 40);
        ServiceEditDTO editDTO = new ServiceEditDTO();
        editDTO.setId(10L);
        editDTO.setTimeChronos(25);

        when(userService.getLoggedUser(TOKEN_HEADER)).thenReturn(criador);
        when(serviceRepository.findById(10L)).thenReturn(Optional.of(service));
        when(serviceRepository.save(service)).thenReturn(service);

        ServiceEntity atualizado = serviceService.put(editDTO, TOKEN_HEADER);

        assertEquals(25, atualizado.getTimeChronos());
        verify(userService).buyChronos(TOKEN_HEADER, 15);
        verify(notificationService).create("Pedido editado", criador, service);
    }

    @Test
    void deveRetornarErroQuandoNaoProprietarioTentarEditarPedido() {
        ServiceEntity service = criarServico(10L, criador, null, ServiceStatus.CRIADO, 20);
        ServiceEditDTO editDTO = new ServiceEditDTO();
        editDTO.setId(10L);
        editDTO.setTitle("Titulo indevido");

        when(userService.getLoggedUser(TOKEN_HEADER)).thenReturn(prestador);
        when(serviceRepository.findById(10L)).thenReturn(Optional.of(service));

        assertThrows(AuthException.class, () -> serviceService.put(editDTO, TOKEN_HEADER));

        verify(serviceRepository, never()).save(any());
        verify(notificationService, never()).create(any(), any(), any());
    }

    @Test
    void deveAceitarPedidoComSucessoGerandoCodigoDeVerificacao() {
        ServiceEntity service = criarServico(10L, criador, null, ServiceStatus.CRIADO, 20);
        when(userService.getLoggedUser(TOKEN_HEADER)).thenReturn(prestador);
        when(serviceRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(service));
        when(serviceRepository.save(service)).thenReturn(service);

        ServiceEntity aceito = serviceService.acceptService(10L, TOKEN_HEADER);

        assertEquals(ServiceStatus.ACEITO, aceito.getStatus());
        assertSame(prestador, aceito.getUserAccepted());
        assertNotNull(aceito.getVerificationCode());
        assertTrue(aceito.getVerificationCode().matches("\\d{4}"));
        assertNotNull(aceito.getVerificationCodeExpiresAt());
        verify(notificationService).create("Pedido aceito", prestador, service);
        verify(notificationService).create("Pedido aceito por Bruno", criador, service);
    }

    @Test
    void deveRetornarErroQuandoUsuarioTentarAceitarProprioPedido() {
        ServiceEntity service = criarServico(10L, criador, null, ServiceStatus.CRIADO, 20);
        when(userService.getLoggedUser(TOKEN_HEADER)).thenReturn(criador);
        when(serviceRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(service));

        assertThrows(AuthException.class, () -> serviceService.acceptService(10L, TOKEN_HEADER));

        verify(serviceRepository, never()).save(any());
    }

    @Test
    void deveIniciarPedidoComCodigoCorreto() {
        ServiceEntity service = criarServico(10L, criador, prestador, ServiceStatus.ACEITO, 20);
        service.setVerificationCode("1234");
        service.setVerificationCodeExpiresAt(LocalDateTime.now().plusMinutes(1));
        when(userService.getLoggedUser(TOKEN_HEADER)).thenReturn(prestador);
        when(serviceRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(service));
        when(serviceRepository.save(service)).thenReturn(service);

        ServiceEntity iniciado = serviceService.startService(10L, TOKEN_HEADER, "\"1234\"");

        assertEquals(ServiceStatus.EM_ANDAMENTO, iniciado.getStatus());
        assertNull(iniciado.getVerificationCode());
        assertNull(iniciado.getVerificationCodeExpiresAt());
        verify(notificationService).create("Pedido iniciado", prestador, service);
        verify(notificationService).create("Pedido iniciado", criador, service);
    }

    @Test
    void deveIniciarPedidoComCodigoCorretoEmJson() {
        ServiceEntity service = criarServico(10L, criador, prestador, ServiceStatus.ACEITO, 20);
        service.setVerificationCode("1234");
        service.setVerificationCodeExpiresAt(LocalDateTime.now().plusMinutes(1));
        when(userService.getLoggedUser(TOKEN_HEADER)).thenReturn(prestador);
        when(serviceRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(service));
        when(serviceRepository.save(service)).thenReturn(service);

        ServiceEntity iniciado = serviceService.startService(10L, TOKEN_HEADER, "{\"code\":\"1234\"}");

        assertEquals(ServiceStatus.EM_ANDAMENTO, iniciado.getStatus());
        assertNull(iniciado.getVerificationCode());
        assertNull(iniciado.getVerificationCodeExpiresAt());
        verify(notificationService).create("Pedido iniciado", prestador, service);
        verify(notificationService).create("Pedido iniciado", criador, service);
    }

    @Test
    void deveRetornarErroQuandoCodigoDeVerificacaoForIncorreto() {
        ServiceEntity service = criarServico(10L, criador, prestador, ServiceStatus.ACEITO, 20);
        service.setVerificationCode("1234");
        service.setVerificationCodeExpiresAt(LocalDateTime.now().plusMinutes(1));
        when(userService.getLoggedUser(TOKEN_HEADER)).thenReturn(prestador);
        when(serviceRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(service));

        assertThrows(IncorrectValidationCodeException.class,
                () -> serviceService.startService(10L, TOKEN_HEADER, "9999"));

        verify(serviceRepository, never()).save(any());
    }

    @Test
    void deveReabrirPedidoQuandoCodigoDeVerificacaoExpirar() {
        ServiceEntity service = criarServico(10L, criador, prestador, ServiceStatus.ACEITO, 20);
        service.setVerificationCode("1234");
        service.setVerificationCodeExpiresAt(LocalDateTime.now().minusMinutes(1));
        when(userService.getLoggedUser(TOKEN_HEADER)).thenReturn(prestador);
        when(serviceRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(service));
        when(serviceRepository.save(service)).thenReturn(service);

        assertThrows(ExpiredValidationCodeException.class,
                () -> serviceService.startService(10L, TOKEN_HEADER, "1234"));

        assertEquals(ServiceStatus.CRIADO, service.getStatus());
        assertNull(service.getUserAccepted());
        assertNull(service.getVerificationCode());
        verify(notificationService).create("Tempo para iniciar o pedido expirou", criador, service);
        verify(notificationService).create("Tempo para iniciar o pedido expirou", prestador, service);
    }

    @Test
    void deveExpirarAceiteDePedidoQuandoPrazoVencido() {
        ServiceEntity service = criarServico(10L, criador, prestador, ServiceStatus.ACEITO, 20);
        service.setVerificationCode("1234");
        service.setVerificationCodeExpiresAt(LocalDateTime.now().minusMinutes(1));
        when(userService.getLoggedUser(TOKEN_HEADER)).thenReturn(criador);
        when(serviceRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(service));
        when(serviceRepository.save(service)).thenReturn(service);

        ServiceEntity expirado = serviceService.expireAcceptedService(10L, TOKEN_HEADER);

        assertSame(service, expirado);
        assertEquals(ServiceStatus.CRIADO, expirado.getStatus());
        assertNull(expirado.getUserAccepted());
        assertNull(expirado.getVerificationCode());
        verify(notificationService).create("Tempo para iniciar o pedido expirou", criador, service);
        verify(notificationService).create("Tempo para iniciar o pedido expirou", prestador, service);
    }

    @Test
    void deveFinalizarPedidoComSucessoEDispararNotificacoes() {
        ServiceEntity service = criarServico(10L, criador, prestador, ServiceStatus.EM_ANDAMENTO, 20);
        service.setVerificationCode("1234");
        service.setVerificationCodeExpiresAt(LocalDateTime.now().plusMinutes(1));
        when(userService.getLoggedUser(TOKEN_HEADER)).thenReturn(criador);
        when(serviceRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(service));
        when(serviceRepository.save(service)).thenReturn(service);

        ServiceEntity finalizado = serviceService.finishService(10L, TOKEN_HEADER);

        assertEquals(ServiceStatus.CONCLUIDO, finalizado.getStatus());
        assertNull(finalizado.getVerificationCode());
        assertNull(finalizado.getVerificationCodeExpiresAt());
        verify(notificationService).create("Pedido finalizado", criador, service);
        verify(notificationService).create("Pedido finalizado", prestador, service);
    }

    @Test
    void deveCancelarPedidoComoProprietarioEDispararNotificacoesParaAsPartes() {
        ServiceEntity service = criarServico(10L, criador, prestador, ServiceStatus.EM_ANDAMENTO, 20);
        service.setVerificationCode("1234");
        service.setVerificationCodeExpiresAt(LocalDateTime.now().plusMinutes(1));
        when(userService.getLoggedUser(TOKEN_HEADER)).thenReturn(criador);
        when(serviceRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(service));
        when(serviceRepository.save(service)).thenReturn(service);

        ServiceEntity cancelado = serviceService.cancelService(10L, TOKEN_HEADER);

        assertEquals(ServiceStatus.CANCELADO, cancelado.getStatus());
        assertNull(cancelado.getVerificationCode());
        assertNull(cancelado.getVerificationCodeExpiresAt());
        verify(notificationService).create("Pedido cancelado", criador, service);
        verify(notificationService).create("Pedido cancelado por Ana", prestador, service);
    }

    @Test
    void deveRetornarErroQuandoUsuarioSemVinculoTentarCancelarPedido() {
        UserEntity terceiro = criarUsuario(3L, "Carlos", 50);
        ServiceEntity service = criarServico(10L, criador, prestador, ServiceStatus.EM_ANDAMENTO, 20);
        when(userService.getLoggedUser(TOKEN_HEADER)).thenReturn(terceiro);
        when(serviceRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(service));

        assertThrows(AuthException.class, () -> serviceService.cancelService(10L, TOKEN_HEADER));

        verify(serviceRepository, never()).save(any());
        verify(notificationService, never()).create(any(), any(), any());
    }

    @Test
    void deveExcluirPedidoCriadoEDevolverChronosAoProprietario() {
        ServiceEntity service = criarServico(10L, criador, null, ServiceStatus.CRIADO, 20);
        when(userService.getLoggedUser(TOKEN_HEADER)).thenReturn(criador);
        when(serviceRepository.findById(10L)).thenReturn(Optional.of(service));

        serviceService.deleteService(10L, TOKEN_HEADER);

        verify(userService).buyChronos(TOKEN_HEADER, 20);
        verify(notificationService).deleteByService(service);
        verify(serviceRepository).delete(service);
    }

    @Test
    void deveRetornarErroQuandoExcluirPedidoConcluido() {
        ServiceEntity service = criarServico(10L, criador, prestador, ServiceStatus.CONCLUIDO, 20);
        when(userService.getLoggedUser(TOKEN_HEADER)).thenReturn(criador);
        when(serviceRepository.findById(10L)).thenReturn(Optional.of(service));

        assertThrows(AuthException.class, () -> serviceService.deleteService(10L, TOKEN_HEADER));

        verify(userService, never()).buyChronos(any(), any());
        verify(serviceRepository, never()).delete(any(ServiceEntity.class));
    }

    @Test
    void deveListarPedidosPorStatusComPaginacao() {
        when(userService.getLoggedUser(TOKEN_HEADER)).thenReturn(criador);
        when(serviceRepository.findAllByStatus(eq(ServiceStatus.CRIADO), any()))
                .thenReturn(org.springframework.data.domain.Page.empty());

        var page = serviceService.getAllByStatus(ServiceStatus.CRIADO, TOKEN_HEADER, 0, 10);

        assertFalse(page.hasContent());
        verify(userService).getLoggedUser(TOKEN_HEADER);
        verify(serviceRepository).findAllByStatus(eq(ServiceStatus.CRIADO), any());
    }

    @Test
    void devePersistirServicoMontadoNaCriacao() {
        ServiceDTO dto = criarServiceDTO(15);
        when(userService.getLoggedUser(TOKEN_HEADER)).thenReturn(criador);
        when(storageService.uploadBase64Image(eq("base64-imagem"), eq("services"), isNull()))
                .thenReturn("https://storage/servico.png");
        when(serviceRepository.save(any(ServiceEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        serviceService.create(dto, TOKEN_HEADER);

        ArgumentCaptor<ServiceEntity> serviceCaptor = ArgumentCaptor.forClass(ServiceEntity.class);
        verify(serviceRepository).save(serviceCaptor.capture());
        ServiceEntity salvo = serviceCaptor.getValue();
        assertEquals("Aula de Java", salvo.getTitle());
        assertEquals(15, salvo.getTimeChronos());
        assertEquals(ServiceStatus.CRIADO, salvo.getStatus());
        assertSame(criador, salvo.getUserCreator());
        verify(notificationService, times(1)).create("Pedido criado", criador, salvo);
    }

    private ServiceDTO criarServiceDTO(int chronos) {
        return new ServiceDTO(
                "Aula de Java",
                "Mentoria para projeto Spring Boot",
                chronos,
                "Remoto",
                LocalDate.now().plusDays(15),
                List.of("Programacao", "Backend"),
                "base64-imagem"
        );
    }

    private ServiceEntity criarServico(
            Long id,
            UserEntity userCreator,
            UserEntity userAccepted,
            ServiceStatus status,
            int chronos
    ) {
        ServiceEntity service = new ServiceEntity();
        service.setId(id);
        service.setTitle("Aula de Java");
        service.setDescription("Mentoria para projeto Spring Boot");
        service.setTimeChronos(chronos);
        service.setDeadline(LocalDate.now().plusDays(15));
        service.setModality(ServiceModality.REMOTO);
        service.setPostedAt(LocalDateTime.now().minusDays(1));
        service.setStatus(status);
        service.setUserCreator(userCreator);
        service.setUserAccepted(userAccepted);
        return service;
    }

    private UserEntity criarUsuario(Long id, String nome, int chronos) {
        UserEntity user = new UserEntity();
        user.setId(id);
        user.setName(nome);
        user.setEmail(nome.toLowerCase() + "@chronora.com");
        user.setPhoneNumber(11999999990L + id);
        user.setTimeChronos(chronos);
        user.setSupabaseUserId("supabase-" + id);
        return user;
    }
}
