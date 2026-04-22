package br.com.senai.controller;

import br.com.senai.model.DTO.ApiResponse;
import br.com.senai.model.DTO.ServiceDTO;
import br.com.senai.model.DTO.ServiceEditDTO;
import br.com.senai.model.entity.ServiceEntity;
import br.com.senai.model.enums.ServiceStatus;
import br.com.senai.service.ServiceService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/service")
@Validated
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
        logger.info("Iniciando criacao de servico");
        ServiceEntity saved = serviceService.create(serviceDTO, tokenHeader);
        logger.info("Servico criado com id {}", saved.getId());
        return ResponseEntity.ok(saved);
    }

    @PutMapping("/put")
    public ResponseEntity<ServiceEntity> put(
            @RequestHeader("Authorization") String tokenHeader,
            @RequestBody @Valid ServiceEditDTO serviceEditDTO
    ) {
        logger.info("Editando servico {}", serviceEditDTO.getId());
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

    @PutMapping("/expireAcceptedService/{id}")
    public ResponseEntity<ServiceEntity> expireAcceptedService(
            @RequestHeader("Authorization") String tokenHeader,
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(serviceService.expireAcceptedService(id, tokenHeader));
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

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteService(
            @RequestHeader("Authorization") String tokenHeader,
            @PathVariable Long id
    ) {
        serviceService.deleteService(id, tokenHeader);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/get/{id}")
    public ResponseEntity<ServiceEntity> getById(@PathVariable Long id) {
        logger.info("Buscando servico por id {}", id);
        ServiceEntity service = serviceService.getById(id);
        return ResponseEntity.ok(service);
    }

    @GetMapping("/get/all")
    public ResponseEntity<ApiResponse<List<ServiceEntity>>> getAll(
            @RequestHeader("Authorization") String tokenHeader,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) int size
    ) {
        Page<ServiceEntity> services = serviceService.getAll(tokenHeader, page, size);
        return ResponseEntity.ok(ApiResponse.ofPage(
                services.getContent(),
                page,
                size,
                services.getTotalElements(),
                services.getTotalPages()
        ));
    }

    @GetMapping("/get/all/{status}")
    public ResponseEntity<ApiResponse<List<ServiceEntity>>> getAllByStatus(
            @PathVariable ServiceStatus status,
            @RequestHeader("Authorization") String tokenHeader,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) int size
    ) {
        Page<ServiceEntity> services = serviceService.getAllByStatus(status, tokenHeader, page, size);
        return ResponseEntity.ok(ApiResponse.ofPage(
                services.getContent(),
                page,
                size,
                services.getTotalElements(),
                services.getTotalPages()
        ));
    }
}
