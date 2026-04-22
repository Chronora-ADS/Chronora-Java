package br.com.senai.controller;

import br.com.senai.model.DTO.ClientLogDTO;
import br.com.senai.service.MonitoringService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/monitoring")
public class MonitoringController {

    private final MonitoringService monitoringService;

    public MonitoringController(MonitoringService monitoringService) {
        this.monitoringService = monitoringService;
    }

    @PostMapping("/client-logs")
    public ResponseEntity<Void> relayClientLogs(@Valid @RequestBody ClientLogDTO payload) {
        monitoringService.relayClientLog(payload);
        return ResponseEntity.ok().build();
    }
}
