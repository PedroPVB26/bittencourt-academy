package dev.pedrobittencourt.email_service.exception;

public class InvalidEmailTypeException extends RuntimeException {
    public InvalidEmailTypeException(String emailType) {
        super("Invalid Email Type: " + emailType);
    }
}
