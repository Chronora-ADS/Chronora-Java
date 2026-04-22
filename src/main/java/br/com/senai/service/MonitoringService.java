package br.com.senai.service;

import br.com.senai.exception.SupabaseIntegrationException;
import br.com.senai.model.DTO.ClientLogDTO;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Service
public class MonitoringService {

    private static final String BETTER_STACK_INGEST_URL = "https://in.logs.betterstack.com";

    private final RestTemplate restTemplate;

    @Value("${betterstack.source-token:}")
    private String betterStackSourceToken;

    public MonitoringService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public void relayClientLog(ClientLogDTO logDTO) {
        if (!StringUtils.hasText(betterStackSourceToken)) {
            return;
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + betterStackSourceToken);

        Map<String, Object> payload = new HashMap<>();
        payload.put("level", logDTO.getLevel());
        payload.put("source", logDTO.getSource());
        payload.put("message", logDTO.getMessage());
        payload.put("stackTrace", logDTO.getStackTrace());
        payload.put("platform", logDTO.getPlatform());
        payload.put("isReleaseMode", logDTO.getIsReleaseMode());
        payload.put("context", logDTO.getContext());
        payload.put(
                "timestamp",
                StringUtils.hasText(logDTO.getTimestamp()) ? logDTO.getTimestamp() : java.time.OffsetDateTime.now().toString()
        );

        try {
            restTemplate.exchange(
                    BETTER_STACK_INGEST_URL,
                    HttpMethod.POST,
                    new HttpEntity<>(payload, headers),
                    String.class
            );
        } catch (RestClientException e) {
            throw new SupabaseIntegrationException("Falha ao enviar log para Better Stack", e);
        }
    }
}
