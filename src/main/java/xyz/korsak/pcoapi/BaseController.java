package xyz.korsak.pcoapi;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;

@CrossOrigin
@Slf4j
public class BaseController {
    protected String extractBearerToken(String authorizationHeader) {
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            return authorizationHeader.substring(7);
        }
        throw new IllegalArgumentException("Invalid Authorization header format");
    }

    protected <T> ResponseEntity<T> logResponse(ResponseEntity<T> responseEntity) {
        if (responseEntity != null) {
            log.debug("rb: {}", responseEntity.getBody());
        }
        return responseEntity;
    }
}
