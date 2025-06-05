package xyz.korsak.pcoapi.health;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
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

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

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
        
        try {
            // Test Redis connectivity
            redisTemplate.opsForValue().set("health-check", "ping");
            String response = (String) redisTemplate.opsForValue().get("health-check");
            redisTemplate.delete("health-check");
            
            if ("ping".equals(response)) {
                readiness.put("status", "READY");
                readiness.put("timestamp", Instant.now().toString());
                readiness.put("message", "Service is ready to accept requests");
                readiness.put("redis", "Connected");
                return logResponse(ResponseEntity.ok(readiness));
            } else {
                readiness.put("status", "NOT_READY");
                readiness.put("timestamp", Instant.now().toString());
                readiness.put("message", "Redis connection test failed");
                readiness.put("redis", "Disconnected");
                return logResponse(ResponseEntity.status(503).body(readiness));
            }
        } catch (Exception e) {
            log.warn("Health check failed", e);
            readiness.put("status", "NOT_READY");
            readiness.put("timestamp", Instant.now().toString());
            readiness.put("message", "Redis connection failed: " + e.getMessage());
            readiness.put("redis", "Error");
            return logResponse(ResponseEntity.status(503).body(readiness));
        }
    }

    @GetMapping("/actuator/health")
    public ResponseEntity<Map<String, Object>> actuatorHealth() {
        Map<String, Object> health = new HashMap<>();
        
        try {
            // Test Redis connectivity
            redisTemplate.opsForValue().set("health-check", "ping");
            String response = (String) redisTemplate.opsForValue().get("health-check");
            redisTemplate.delete("health-check");
            
            if ("ping".equals(response)) {
                health.put("status", "UP");
                
                // Add components detail
                Map<String, Object> components = new HashMap<>();
                Map<String, Object> redis = new HashMap<>();
                redis.put("status", "UP");
                redis.put("details", Map.of("connection", "active"));
                components.put("redis", redis);
                health.put("components", components);
                
                return logResponse(ResponseEntity.ok(health));
            } else {
                health.put("status", "DOWN");
                return logResponse(ResponseEntity.status(503).body(health));
            }
        } catch (Exception e) {
            log.warn("Actuator health check failed", e);
            health.put("status", "DOWN");
            return logResponse(ResponseEntity.status(503).body(health));
        }
    }
}