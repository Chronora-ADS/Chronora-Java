package br.com.senai.controller;

import br.com.senai.model.DTO.ServiceDTO;
import br.com.senai.model.DTO.ServiceEditDTO;
import br.com.senai.model.entity.ServiceEntity;
import br.com.senai.service.ServiceService;
import jakarta.validation.Valid;
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

import java.util.List;

@RestController
@RequestMapping("/service")
public class ServiceController {

    private final ServiceService serviceService;
    private static final Logger logger = LoggerFactory.getLogger(ServiceController.class);

    public ServiceController(ServiceService serviceService) {
        this.serviceService = serviceService;
    }

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
    public ResponseEntity<ServiceEntity> put(@RequestHeader("Authorization") String tokenHeader, @RequestBody @Valid ServiceEditDTO serviceEditDTO) {
        logger.info("Editando serviço: {}", serviceEditDTO);
        ServiceEntity saved = serviceService.put(serviceEditDTO, tokenHeader);
        logger.info("Serviço editado com sucesso: {}", saved.getId());
        return ResponseEntity.ok(saved);
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
}
