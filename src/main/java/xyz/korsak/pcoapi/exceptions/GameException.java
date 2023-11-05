package xyz.korsak.pcoapi.exceptions;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GameException extends RuntimeException {
    public GameException(String message) {
        super(message);
        log.warn("GameException: {}", message);
    }
}
