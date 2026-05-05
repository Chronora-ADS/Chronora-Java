package br.com.senai.controller;

import java.time.Instant;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

    private final String appName;
    private final String appVersion;
    private final String gitCommit;

    public HealthController(
            @Value("${build.name:Chronora-Java}") String appName,
            @Value("${build.version:unknown}") String appVersion,
            @Value("${git.commit.id.abbrev:unknown}") String gitCommit
    ) {
        this.appName = appName;
        this.appVersion = appVersion;
        this.gitCommit = gitCommit;
    }

    @RequestMapping(value = "/health", method = RequestMethod.HEAD)
    public ResponseEntity<Void> healthHead() {
        return ResponseEntity.ok().build();
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        return ResponseEntity.ok(Map.of(
                "app", appName,
                "version", appVersion,
                "commit", gitCommit,
                "status", "UP",
                "timestamp", Instant.now().toString()
        ));
    }

    @GetMapping("/healthz")
    public ResponseEntity<String> healthz() {
        return ResponseEntity.ok("OK");
    }
}
