package br.com.senai.controller;

import br.com.senai.model.DTO.RequestDTO;
import br.com.senai.model.entity.ServiceEntity;
import br.com.senai.service.RequestService;
import br.com.senai.service.ServiceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/requests")
@RequiredArgsConstructor
public class RequestController {

    private final RequestService requestService;
    private final ServiceService serviceService;

    @PostMapping("/create")
    public ResponseEntity<ServiceEntity> createRequest(@RequestHeader("Authorization") String tokenHeader, @RequestBody RequestDTO requestDTO) {
        ServiceEntity saved = requestService.createFromRequest(requestDTO, tokenHeader);
        return ResponseEntity.ok(saved);
    }

    @GetMapping("/get/{id}")
    public ResponseEntity<ServiceEntity> getRequestById(@PathVariable Long id) {
        ServiceEntity service = serviceService.getById(id);
        return ResponseEntity.ok(service);
    }

    @GetMapping("/get/all")
    public ResponseEntity<List<ServiceEntity>> getAllRequests(@RequestHeader("Authorization") String tokenHeader) {
        return ResponseEntity.ok(serviceService.getAll(tokenHeader));
    }
}