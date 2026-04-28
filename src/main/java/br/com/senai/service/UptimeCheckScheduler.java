package br.com.senai.service;

import br.com.senai.model.entity.UptimeCheckEntity;
import br.com.senai.repository.UptimeCheckRepository;
import java.time.Instant;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class UptimeCheckScheduler {

    private static final Logger logger = LoggerFactory.getLogger(UptimeCheckScheduler.class);

    private final UptimeCheckRepository uptimeCheckRepository;
    private final RestTemplate restTemplate = new RestTemplate();
    private final int serverPort;
    private final String healthPath;

    public UptimeCheckScheduler(
            UptimeCheckRepository uptimeCheckRepository,
            @Value("${server.port:8080}") int serverPort,
            @Value("${uptime.check.path:/health}") String healthPath
    ) {
        this.uptimeCheckRepository = uptimeCheckRepository;
        this.serverPort = serverPort;
        this.healthPath = healthPath;
    }

    @Scheduled(
            fixedDelayString = "${uptime.check.delay-ms:60000}",
            initialDelayString = "${uptime.check.initial-delay-ms:10000}"
    )
    public void checkHealth() {
        String url = "http://localhost:" + serverPort + healthPath;
        long start = System.nanoTime();
        UptimeCheckEntity check = new UptimeCheckEntity();
        check.setCheckedAt(Instant.now());

        try {
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            long elapsedMs = (System.nanoTime() - start) / 1_000_000;
            Object statusValue = response.getBody() != null ? response.getBody().get("status") : null;
            String status = statusValue != null ? statusValue.toString() : "UNKNOWN";

            check.setStatusCode(response.getStatusCodeValue());
            check.setStatus(status);
            check.setResponseTimeMs(elapsedMs);
            uptimeCheckRepository.save(check);

            logger.info("Uptime check OK: status={}, code={}, ms={}", status, response.getStatusCodeValue(), elapsedMs);
        } catch (Exception e) {
            long elapsedMs = (System.nanoTime() - start) / 1_000_000;
            check.setStatus("DOWN");
            check.setResponseTimeMs(elapsedMs);
            check.setErrorMessage(e.getMessage());
            uptimeCheckRepository.save(check);

            logger.warn("Uptime check FAILED: ms={}, error={}", elapsedMs, e.getMessage());
        }
    }
}
