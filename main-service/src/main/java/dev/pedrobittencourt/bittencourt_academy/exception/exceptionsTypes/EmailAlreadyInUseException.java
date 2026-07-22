package dev.pedrobittencourt.bittencourt_academy.exception.exceptionsTypes;

public class EmailAlreadyInUseException extends RuntimeException {
    public EmailAlreadyInUseException() {
        super("Email already in use");
    }
}