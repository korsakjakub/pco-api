package xyz.korsak.pcoapi.health;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import xyz.korsak.pcoapi.BaseController;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/health")
@Slf4j
public class HealthController extends BaseController {

    @GetMapping
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("timestamp", Instant.now().toString());
        health.put("service", "pco-api");
        health.put("version", "1.0.0");
        
        return logResponse(ResponseEntity.ok(health));
    }

    @GetMapping("/ready")
    public ResponseEntity<Map<String, Object>> readiness() {
        Map<String, Object> readiness = new HashMap<>();
        readiness.put("status", "READY");
        readiness.put("timestamp", Instant.now().toString());
        readiness.put("message", "Service is ready to accept requests");
        
        return logResponse(ResponseEntity.ok(readiness));
    }
}