package br.com.senai.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ServiceDeadlineScheduler {

    private final ServiceService serviceService;

    public ServiceDeadlineScheduler(ServiceService serviceService) {
        this.serviceService = serviceService;
    }

    @Scheduled(
            cron = "${service.deadline.check-cron:0 5 0 * * *}",
            zone = "${service.deadline.check-zone:America/Sao_Paulo}"
    )
    public void processDeadlineRules() {
        serviceService.processDeadlineRules();
    }
}
