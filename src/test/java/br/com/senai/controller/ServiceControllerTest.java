package br.com.senai.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.com.senai.model.DTO.service.ServiceCancellationDTO;
import br.com.senai.model.DTO.service.ServiceDTO;
import br.com.senai.model.DTO.service.ServiceDeadlineRenewalDTO;
import br.com.senai.model.DTO.service.ServiceEditDTO;
import br.com.senai.model.entity.ServiceEntity;
import br.com.senai.model.enums.ServiceModality;
import br.com.senai.model.enums.ServiceStatus;
import br.com.senai.service.service.ServiceService;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

@ExtendWith(MockitoExtension.class)
class ServiceControllerTest {

    private static final String TOKEN_HEADER = "Bearer token-valido";

    @Mock
    private ServiceService serviceService;

    @InjectMocks
    private ServiceController serviceController;

    @Test
    void deveCriarPedidoViaController() {
        ServiceDTO dto = criarServiceDTO();
        ServiceEntity service = criarServico(10L, ServiceStatus.CRIADO);
        when(serviceService.create(dto, TOKEN_HEADER)).thenReturn(service);

        var response = serviceController.create(TOKEN_HEADER, dto);

        assertEquals(200, response.getStatusCode().value());
        assertSame(service, response.getBody());
        verify(serviceService).create(dto, TOKEN_HEADER);
    }

    @Test
    void deveEditarPedidoViaController() {
        ServiceEditDTO dto = new ServiceEditDTO();
        dto.setId(10L);
        dto.setTitle("Aula atualizada");
        ServiceEntity service = criarServico(10L, ServiceStatus.CRIADO);
        when(serviceService.put(dto, TOKEN_HEADER)).thenReturn(service);

        var response = serviceController.put(TOKEN_HEADER, dto);

        assertEquals(200, response.getStatusCode().value());
        assertSame(service, response.getBody());
        verify(serviceService).put(dto, TOKEN_HEADER);
    }

    @Test
    void deveFinalizarPedidoViaController() {
        ServiceEntity service = criarServico(10L, ServiceStatus.CONCLUIDO);
        when(serviceService.finishService(10L, TOKEN_HEADER)).thenReturn(service);

        var response = serviceController.finishService(TOKEN_HEADER, 10L);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(ServiceStatus.CONCLUIDO, response.getBody().getStatus());
        verify(serviceService).finishService(10L, TOKEN_HEADER);
    }

    @Test
    void deveAceitarPedidoViaController() {
        ServiceEntity service = criarServico(10L, ServiceStatus.ACEITO);
        when(serviceService.acceptService(10L, TOKEN_HEADER)).thenReturn(service);

        var response = serviceController.acceptService(TOKEN_HEADER, 10L);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(ServiceStatus.ACEITO, response.getBody().getStatus());
        verify(serviceService).acceptService(10L, TOKEN_HEADER);
    }

    @Test
    void deveIniciarPedidoViaController() {
        ServiceEntity service = criarServico(10L, ServiceStatus.EM_ANDAMENTO);
        when(serviceService.startService(10L, TOKEN_HEADER, "1234")).thenReturn(service);

        var response = serviceController.startService(TOKEN_HEADER, 10L, "1234");

        assertEquals(200, response.getStatusCode().value());
        assertEquals(ServiceStatus.EM_ANDAMENTO, response.getBody().getStatus());
        verify(serviceService).startService(10L, TOKEN_HEADER, "1234");
    }

    @Test
    void deveExpirarAceiteDePedidoViaController() {
        ServiceEntity service = criarServico(10L, ServiceStatus.CRIADO);
        when(serviceService.expireAcceptedService(10L, TOKEN_HEADER)).thenReturn(service);

        var response = serviceController.expireAcceptedService(TOKEN_HEADER, 10L);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(ServiceStatus.CRIADO, response.getBody().getStatus());
        verify(serviceService).expireAcceptedService(10L, TOKEN_HEADER);
    }

    @Test
    void deveIniciarSegundaChamadaViaController() {
        ServiceEntity service = criarServico(10L, ServiceStatus.ACEITO);
        when(serviceService.secondCall(10L, TOKEN_HEADER)).thenReturn(service);

        var response = serviceController.secondCall(TOKEN_HEADER, 10L);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(ServiceStatus.ACEITO, response.getBody().getStatus());
        verify(serviceService).secondCall(10L, TOKEN_HEADER);
    }

    @Test
    void deveCancelarServicoAceitoViaController() {
        ServiceEntity service = criarServico(10L, ServiceStatus.CRIADO);
        when(serviceService.cancelAcceptedService(10L, TOKEN_HEADER, null)).thenReturn(service);

        var response = serviceController.cancelAcceptedService(TOKEN_HEADER, 10L, null);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(ServiceStatus.CRIADO, response.getBody().getStatus());
        verify(serviceService).cancelAcceptedService(10L, TOKEN_HEADER, null);
    }

    @Test
    void deveRegistrarJustificativaDeCancelamentoViaController() {
        ServiceCancellationDTO dto = new ServiceCancellationDTO();
        dto.setJustification("Nao houve retorno no prazo combinado.");
        ServiceEntity service = criarServico(10L, ServiceStatus.CRIADO);
        when(serviceService.registerServiceCancellationJustification(10L, TOKEN_HEADER, dto)).thenReturn(service);

        var response = serviceController.registerServiceCancellationJustification(TOKEN_HEADER, 10L, dto);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(ServiceStatus.CRIADO, response.getBody().getStatus());
        verify(serviceService).registerServiceCancellationJustification(10L, TOKEN_HEADER, dto);
    }

    @Test
    void deveCancelarPedidoViaController() {
        ServiceEntity service = criarServico(10L, ServiceStatus.CANCELADO);
        when(serviceService.cancelService(10L, TOKEN_HEADER)).thenReturn(service);

        var response = serviceController.cancelService(TOKEN_HEADER, 10L);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(ServiceStatus.CANCELADO, response.getBody().getStatus());
        verify(serviceService).cancelService(10L, TOKEN_HEADER);
    }

    @Test
    void deveRenovarPrazoViaController() {
        ServiceDeadlineRenewalDTO dto = new ServiceDeadlineRenewalDTO();
        dto.setDeadline(LocalDate.now().plusDays(5));
        ServiceEntity service = criarServico(10L, ServiceStatus.CRIADO);
        service.setDeadline(dto.getDeadline());

        when(serviceService.renewDeadline(10L, TOKEN_HEADER, dto.getDeadline())).thenReturn(service);

        var response = serviceController.renewDeadline(TOKEN_HEADER, 10L, dto);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(dto.getDeadline(), response.getBody().getDeadline());
        verify(serviceService).renewDeadline(10L, TOKEN_HEADER, dto.getDeadline());
    }

    @Test
    void deveBuscarPedidoPorIdViaController() {
        ServiceEntity service = criarServico(10L, ServiceStatus.CRIADO);
        when(serviceService.getById(10L)).thenReturn(service);

        var response = serviceController.getById(10L);

        assertEquals(200, response.getStatusCode().value());
        assertSame(service, response.getBody());
        verify(serviceService).getById(10L);
    }

    @Test
    void deveListarPedidosPaginadosViaController() {
        ServiceEntity service = criarServico(10L, ServiceStatus.CRIADO);
        when(serviceService.getAll(TOKEN_HEADER, 0, 10)).thenReturn(new PageImpl<>(List.of(service), PageRequest.of(0, 10), 1));

        var response = serviceController.getAll(TOKEN_HEADER, 0, 10);
        Page<ServiceEntity> page = response.getBody();

        assertEquals(200, response.getStatusCode().value());
        assertEquals(List.of(service), page.getContent());
        assertEquals(0, page.getNumber());
        assertEquals(10, page.getSize());
        assertEquals(1L, page.getTotalElements());
        verify(serviceService).getAll(TOKEN_HEADER, 0, 10);
    }

    @Test
    void deveListarPedidosPorStatusViaController() {
        ServiceEntity service = criarServico(10L, ServiceStatus.ACEITO);
        when(serviceService.filterServices(ServiceStatus.ACEITO, TOKEN_HEADER, 0, 10, null, null, null, null, null, null, "0"))
                .thenReturn(new PageImpl<>(List.of(service), PageRequest.of(0, 10), 1));

        var response = serviceController.getAllByStatus(ServiceStatus.ACEITO, TOKEN_HEADER, 0, 10, null, null, null, null, null, null, "0");
        Page<ServiceEntity> page = response.getBody();

        assertEquals(200, response.getStatusCode().value());
        assertEquals(List.of(service), page.getContent());
        assertEquals(0, page.getNumber());
        assertEquals(10, page.getSize());
        assertEquals(1L, page.getTotalElements());
        verify(serviceService).filterServices(ServiceStatus.ACEITO, TOKEN_HEADER, 0, 10, null, null, null, null, null, null, "0");
    }

    @Test
    void deveExcluirPedidoViaController() {
        var response = serviceController.deleteService(TOKEN_HEADER, 10L);

        assertEquals(200, response.getStatusCode().value());
        verify(serviceService).deleteService(10L, TOKEN_HEADER);
    }

    private ServiceDTO criarServiceDTO() {
        return new ServiceDTO(
                "Aula de Java",
                "Mentoria Spring Boot",
                10,
                "Remoto",
                LocalDate.now().plusDays(10),
                List.of("Programacao"),
                "base64-imagem"
        );
    }

    private ServiceEntity criarServico(Long id, ServiceStatus status) {
        ServiceEntity service = new ServiceEntity();
        service.setId(id);
        service.setTitle("Aula de Java");
        service.setDescription("Mentoria Spring Boot");
        service.setTimeChronos(10);
        service.setDeadline(LocalDate.now().plusDays(10));
        service.setPostedAt(LocalDateTime.now());
        service.setModality(ServiceModality.REMOTO);
        service.setStatus(status);
        return service;
    }
}
