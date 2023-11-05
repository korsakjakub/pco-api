package xyz.korsak.pcoapi.exceptions;

public class UnauthorizedAccessException extends RuntimeException {
    private static final String DEFAULT_MESSAGE = "Unauthorized access";

    public UnauthorizedAccessException() {
        super(DEFAULT_MESSAGE);
    }
}
