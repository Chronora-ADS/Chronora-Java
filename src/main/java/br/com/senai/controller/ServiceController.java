package br.com.senai.controller;

import br.com.senai.model.DTO.ServiceDTO;
import br.com.senai.model.entity.ServiceEntity;
import br.com.senai.service.ServiceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/service")
@RequiredArgsConstructor
public class ServiceController {

    private final ServiceService serviceService;

    @PostMapping("/post/{userId}")
    public ResponseEntity<ServiceEntity> create(@PathVariable Long userId, @RequestBody ServiceDTO serviceDTO) throws Exception {
        ServiceEntity saved = serviceService.create(serviceDTO, userId);
        return ResponseEntity.ok(saved);
    }

    @GetMapping("/get/{id}")
    public ResponseEntity<ServiceEntity> getById(@PathVariable Long id) {
        return serviceService.getById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/get/all")
    public ResponseEntity<List<ServiceEntity>> getAll() {
        return ResponseEntity.ok(serviceService.getAll());
    }
}