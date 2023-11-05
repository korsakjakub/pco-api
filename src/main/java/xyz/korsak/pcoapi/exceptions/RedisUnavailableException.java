package xyz.korsak.pcoapi.exceptions;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RedisUnavailableException extends RuntimeException {
    public RedisUnavailableException(String message) {
        super(message);
        log.error("RedisUnavailableException: {}", message);
    }
}

