package xyz.korsak.pcoapi.health;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuator.health.Health;
import org.springframework.boot.actuator.health.HealthIndicator;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class RedisHealthIndicator implements HealthIndicator {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public Health health() {
        try {
            // Perform Redis ping test
            redisTemplate.opsForValue().set("health-check", "ping");
            String response = (String) redisTemplate.opsForValue().get("health-check");
            redisTemplate.delete("health-check");
            
            if ("ping".equals(response)) {
                return Health.up()
                    .withDetail("redis", "Connection successful")
                    .withDetail("operation", "ping test passed")
                    .build();
            } else {
                return Health.down()
                    .withDetail("redis", "Ping test failed")
                    .withDetail("expected", "ping")
                    .withDetail("actual", response)
                    .build();
            }
        } catch (Exception e) {
            log.warn("Redis health check failed", e);
            return Health.down()
                .withDetail("redis", "Connection failed")
                .withDetail("error", e.getMessage())
                .build();
        }
    }
}