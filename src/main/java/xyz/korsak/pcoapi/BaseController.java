package xyz.korsak.pcoapi;

import org.springframework.web.bind.annotation.CrossOrigin;

@CrossOrigin
public class BaseController {
    protected String extractBearerToken(String authorizationHeader) {
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            return authorizationHeader.substring(7);
        }
        throw new IllegalArgumentException("Invalid Authorization header format");
    }
}
