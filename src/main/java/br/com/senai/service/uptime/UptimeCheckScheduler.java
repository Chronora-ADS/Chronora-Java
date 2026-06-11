package br.com.senai.service.uptime;

import br.com.senai.model.entity.UptimeCheckEntity;
import br.com.senai.repository.UptimeCheckRepository;
import java.time.Instant;
import java.util.Map;

import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@AllArgsConstructor
public class UptimeCheckScheduler {

    private static final Logger logger = LoggerFactory.getLogger(UptimeCheckScheduler.class);

    private final UptimeCheckRepository uptimeCheckRepository;
    private final RestTemplate restTemplate = new RestTemplate();
    private final int serverPort;
    private final String healthPath;

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
            ParameterizedTypeReference<Map<String, Object>> responseType = new ParameterizedTypeReference<>() {};
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    responseType
            );
            long elapsedMs = (System.nanoTime() - start) / 1_000_000;
            Object statusValue = response.getBody() != null ? response.getBody().get("status") : null;
            String status = statusValue != null ? statusValue.toString() : "UNKNOWN";

            check.setStatusCode(response.getStatusCode().value());
            check.setStatus(status);
            check.setResponseTimeMs(elapsedMs);
            uptimeCheckRepository.save(check);

            logger.info("Uptime check OK: status={}, code={}, ms={}", status, response.getStatusCode().value(), elapsedMs);
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
