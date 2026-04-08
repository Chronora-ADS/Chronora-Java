package br.com.senai.controller;

import br.com.senai.model.DTO.ServiceDTO;
import br.com.senai.model.DTO.ServiceEditDTO;
import br.com.senai.model.entity.ServiceEntity;
import br.com.senai.model.enums.ServiceStatus;
import br.com.senai.service.ServiceService;
import jakarta.validation.Valid;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/service")
public class ServiceController {

    private static final Logger logger = LoggerFactory.getLogger(ServiceController.class);

    private final ServiceService serviceService;

    public ServiceController(ServiceService serviceService) {
        this.serviceService = serviceService;
    }

    @PostMapping("/post")
    public ResponseEntity<ServiceEntity> create(
            @RequestHeader("Authorization") String tokenHeader,
            @RequestBody @Valid ServiceDTO serviceDTO
    ) {
        logger.info("=== INICIANDO CRIACAO DE SERVICO ===");
        logger.info("Token header: {}", tokenHeader != null ? tokenHeader.substring(0, Math.min(20, tokenHeader.length())) + "..." : "NULL");
        logger.info("ServiceDTO recebido: {}", serviceDTO);
        logger.info("Titulo: {}", serviceDTO.getTitle());
        logger.info("Descricao: {}", serviceDTO.getDescription());
        logger.info("TimeChronos: {}", serviceDTO.getTimeChronos());
        logger.info("Deadline: {}", serviceDTO.getDeadline());
        logger.info("Modality: {}", serviceDTO.getModality());
        logger.info("Categories: {}", serviceDTO.getCategories());
        logger.info("ServiceImage: {}", serviceDTO.getServiceImage() != null ? "PRESENTE" : "AUSENTE");

        ServiceEntity saved = serviceService.create(serviceDTO, tokenHeader);

        logger.info("ServiceEntity salvo: {}", saved);
        logger.info("ID do servico salvo: {}", saved.getId());
        logger.info("=== FIM DA CRIACAO DE SERVICO ===");

        return ResponseEntity.ok(saved);
    }

    @PutMapping("/put")
    public ResponseEntity<ServiceEntity> put(
            @RequestHeader("Authorization") String tokenHeader,
            @RequestBody @Valid ServiceEditDTO serviceEditDTO
    ) {
        logger.info("Editando servico: {}", serviceEditDTO);
        ServiceEntity service = serviceService.put(serviceEditDTO, tokenHeader);
        logger.info("Servico editado com sucesso: {}", service.getId());
        return ResponseEntity.ok(service);
    }

    @PutMapping("/acceptService/{id}")
    public ResponseEntity<ServiceEntity> acceptService(
            @RequestHeader("Authorization") String tokenHeader,
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(serviceService.acceptService(id, tokenHeader));
    }

    @PutMapping("/startService/{id}")
    public ResponseEntity<ServiceEntity> startService(
            @RequestHeader("Authorization") String tokenHeader,
            @PathVariable Long id,
            @RequestBody String verificationCode
    ) {
        return ResponseEntity.ok(serviceService.startService(id, tokenHeader, verificationCode));
    }

    @PutMapping("/finishService/{id}")
    public ResponseEntity<ServiceEntity> finishService(
            @RequestHeader("Authorization") String tokenHeader,
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(serviceService.finishService(id, tokenHeader));
    }

    @PutMapping("/cancelService/{id}")
    public ResponseEntity<ServiceEntity> cancelService(
            @RequestHeader("Authorization") String tokenHeader,
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(serviceService.cancelService(id, tokenHeader));
    }

    @GetMapping("/get/{id}")
    public ResponseEntity<ServiceEntity> getById(@PathVariable Long id) {
        logger.info("Buscando servico por ID: {}", id);
        ServiceEntity service = serviceService.getById(id);
        logger.info("Servico encontrado: {}", service);
        return ResponseEntity.ok(service);
    }

    @GetMapping("/get/all")
    public ResponseEntity<List<ServiceEntity>> getAll(@RequestHeader("Authorization") String tokenHeader) {
        logger.info("Listando todos os servicos");
        List<ServiceEntity> services = serviceService.getAll(tokenHeader);
        logger.info("Total de servicos encontrados: {}", services.size());
        return ResponseEntity.ok(services);
    }

    @GetMapping("/get/all/{status}")
    public ResponseEntity<List<ServiceEntity>> getAllByStatus(
            @PathVariable ServiceStatus status,
            @RequestHeader("Authorization") String tokenHeader
    ) {
        logger.info("Listando todos os servicos por status");
        List<ServiceEntity> services = serviceService.getAllByStatus(status, tokenHeader);
        logger.info("Total de servicos encontrados por status: {}", services.size());
        return ResponseEntity.ok(services);
    }
}
