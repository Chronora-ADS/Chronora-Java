package br.com.senai.controller;

import br.com.senai.enums.ServiceStatus;
import br.com.senai.model.DTO.ServiceChangeStatusDTO;
import br.com.senai.model.DTO.ServiceDTO;
import br.com.senai.model.entity.ServiceEntity;
import br.com.senai.service.ServiceService;
import jakarta.validation.Valid;
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
    public ResponseEntity<ServiceEntity> create(@PathVariable Long userId, @RequestBody ServiceDTO serviceDTO) {
        ServiceEntity saved = serviceService.create(serviceDTO, userId);
        return ResponseEntity.ok(saved);
    }

    @GetMapping("/get/{id}")
    public ResponseEntity<ServiceEntity> getById(@PathVariable Long id) {
        ServiceEntity service = serviceService.getById(id);
        return ResponseEntity.ok(service);
    }

    @GetMapping("/get/created")
    public ResponseEntity<List<ServiceEntity>> getAllCreated() {
        return ResponseEntity.ok(serviceService.getAllCreated());
    }

    @GetMapping("/get/initialized")
    public ResponseEntity<List<ServiceEntity>> getAllInitialized() {
        return ResponseEntity.ok(serviceService.getAllInitialized());
    }

    @GetMapping("/get/accepted")
    public ResponseEntity<List<ServiceEntity>> getAllAccepted() {
        return ResponseEntity.ok(serviceService.getAllAccepted());
    }

    @GetMapping("/get/cancelled")
    public ResponseEntity<List<ServiceEntity>> getAllCancelled() {
        return ResponseEntity.ok(serviceService.getAllCancelled());
    }

    @GetMapping("/get/finalized")
    public ResponseEntity<List<ServiceEntity>> getAllFinalized() {
        return ResponseEntity.ok(serviceService.getAllFinalized());
    }

    @GetMapping("/get/all")
    public ResponseEntity<List<ServiceEntity>> getAll() {
        return ResponseEntity.ok(serviceService.getAll());
    }

    @PutMapping("/updateToAcceptedStatus/{id}")
    public ResponseEntity<ServiceEntity> updateToAcceptedStatus(@PathVariable Long id, @Valid @RequestBody ServiceChangeStatusDTO statusDTO) {
        Long userId = get
    }
}