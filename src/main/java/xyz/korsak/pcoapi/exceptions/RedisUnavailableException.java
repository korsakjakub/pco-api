package xyz.korsak.pcoapi.exceptions;

public class RedisUnavailableException extends RuntimeException {
    public RedisUnavailableException(String message) {
        super(message);
    }
}

