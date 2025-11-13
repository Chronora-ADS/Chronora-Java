package br.com.senai.controller;

import br.com.senai.model.DTO.ServiceDTO;
import br.com.senai.model.DTO.ServiceEditDTO;
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

    @PostMapping("/post")
    public ResponseEntity<ServiceEntity> create(@RequestHeader("Authorization") String tokenHeader, @RequestBody ServiceDTO serviceDTO) {
        ServiceEntity saved = serviceService.create(serviceDTO, tokenHeader);
        return ResponseEntity.ok(saved);
    }

    @PutMapping("/put")
    public ResponseEntity<ServiceEntity> put(@RequestHeader("Authorization") String tokenHeader, @RequestBody ServiceEditDTO serviceEditDTO) {
        ServiceEntity saved = serviceService.put(serviceEditDTO, tokenHeader);
        return ResponseEntity.ok(saved);
    }

    @GetMapping("/get/{id}")
    public ResponseEntity<ServiceEntity> getById(@PathVariable Long id) {
        ServiceEntity service = serviceService.getById(id);
        return ResponseEntity.ok(service);
    }

    @GetMapping("/get/all")
    public ResponseEntity<List<ServiceEntity>> getAll() {
        return ResponseEntity.ok(serviceService.getAll());
    }
}