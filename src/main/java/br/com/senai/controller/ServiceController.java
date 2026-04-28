package br.com.senai.controller;

import br.com.senai.model.DTO.ServiceDTO;
import br.com.senai.model.DTO.ServiceEditDTO;
import br.com.senai.model.entity.ServiceEntity;
import br.com.senai.model.enums.ServiceStatus;
import br.com.senai.service.ServiceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/service")
@RequiredArgsConstructor
public class ServiceController {

    private final ServiceService serviceService;
    private static final Logger logger = LoggerFactory.getLogger(ServiceController.class);

    @PostMapping("/post")
    public ResponseEntity<ServiceEntity> create(@RequestHeader("Authorization") String tokenHeader, @RequestBody @Valid ServiceDTO serviceDTO) {
        logger.info("=== INICIANDO CRIAÇÃO DE SERVIÇO ===");
        logger.info("Token header: {}", tokenHeader != null ? tokenHeader.substring(0, Math.min(20, tokenHeader.length())) + "..." : "NULL");
        logger.info("ServiceDTO recebido: {}", serviceDTO);
        logger.info("Título: {}", serviceDTO.getTitle());
        logger.info("Descrição: {}", serviceDTO.getDescription());
        logger.info("TimeChronos: {}", serviceDTO.getTimeChronos());
        logger.info("Deadline: {}", serviceDTO.getDeadline());
        logger.info("Modality: {}", serviceDTO.getModality());
        logger.info("Categories: {}", serviceDTO.getCategories());
        logger.info("ServiceImage: {}", serviceDTO.getServiceImage() != null ? "PRESENTE" : "AUSENTE");

        ServiceEntity saved = serviceService.create(serviceDTO, tokenHeader);

        logger.info("ServiceEntity salvo: {}", saved);
        logger.info("ID do serviço salvo: {}", saved.getId());
        logger.info("=== FIM DA CRIAÇÃO DE SERVIÇO ===");

        return ResponseEntity.ok(saved);
    }

    @PutMapping("/put")
    public ResponseEntity<ServiceEntity> put(@RequestHeader("Authorization") String tokenHeader, @RequestBody ServiceEditDTO serviceEditDTO) {
        logger.info("Editando serviço: {}", serviceEditDTO);
        ServiceEntity service = serviceService.put(serviceEditDTO, tokenHeader);
        logger.info("Serviço editado com sucesso: {}", service.getId());
        return ResponseEntity.ok(service);
    }

    @PutMapping("/acceptService/{id}")
    public ResponseEntity<ServiceEntity> acceptService(@RequestHeader("Authorization") String tokenHeader, @PathVariable Long id) {
        ServiceEntity service = serviceService.acceptService(id, tokenHeader);
        return ResponseEntity.ok(service);
    }

    @PutMapping("/startService/{id}")
    public ResponseEntity<ServiceEntity> startService(@RequestHeader("Authorization") String tokenHeader, @PathVariable Long id, @RequestBody String verificationCode) {
        ServiceEntity service = serviceService.startService(id, tokenHeader, verificationCode);
        return ResponseEntity.ok(service);
    }

    @PutMapping("/expireAcceptedService/{id}")
    public ResponseEntity<ServiceEntity> expireAcceptedService(@RequestHeader("Authorization") String tokenHeader, @PathVariable Long id) {
        ServiceEntity service = serviceService.expireAcceptedService(id, tokenHeader);
        return ResponseEntity.ok(service);
    }

    @PutMapping("/finishService/{id}")
    public ResponseEntity<ServiceEntity> finishService(@RequestHeader("Authorization") String tokenHeader, @PathVariable Long id) {
        ServiceEntity service = serviceService.finishService(id, tokenHeader);
        return ResponseEntity.ok(service);
    }

    @PutMapping("/cancelService/{id}")
    public ResponseEntity<ServiceEntity> cancelService(@RequestHeader("Authorization") String tokenHeader, @PathVariable Long id) {
        ServiceEntity service = serviceService.cancelService(id, tokenHeader);
        return ResponseEntity.ok(service);
    }

    @GetMapping("/get/{id}")
    public ResponseEntity<ServiceEntity> getById(@PathVariable Long id) {
        logger.info("Buscando serviço por ID: {}", id);
        ServiceEntity service = serviceService.getById(id);
        logger.info("Serviço encontrado: {}", service);
        return ResponseEntity.ok(service);
    }

    @GetMapping("/get/all")
    public ResponseEntity<List<ServiceEntity>> getAll(@RequestHeader("Authorization") String tokenHeader) {
        logger.info("Listando todos os serviços");
        List<ServiceEntity> services = serviceService.getAll(tokenHeader);
        logger.info("Total de serviços encontrados: {}", services.size());
        return ResponseEntity.ok(services);
    }

    @GetMapping("/get/all/{status}")
    public ResponseEntity<List<ServiceEntity>> getAllByStatus(@PathVariable ServiceStatus status, @RequestHeader("Authorization") String tokenHeader) {
        logger.info("Listando todos os serviços por status");
        List<ServiceEntity> services = serviceService.getAllByStatus(status, tokenHeader);
        logger.info("Total de serviços encontrados por status: {}", services.size());
        return ResponseEntity.ok(services);
    }
}
