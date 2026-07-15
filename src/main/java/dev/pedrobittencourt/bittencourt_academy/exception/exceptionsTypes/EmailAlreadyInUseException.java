package dev.pedrobittencourt.bittencourt_academy.exception.exceptionsTypes;

public class EmailAlreadyInUseException extends RuntimeException {
    public EmailAlreadyInUseException(String email) {
        super("Email already in use: " + email);
    }
}