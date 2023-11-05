package xyz.korsak.pcoapi.exceptions;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RoomAlreadyExistsException extends RuntimeException {
    public RoomAlreadyExistsException(String message) {
        super(message);
        log.warn("RoomAlreadyExistsException: {}", message);
    }
}

