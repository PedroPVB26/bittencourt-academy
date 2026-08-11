package dev.pedrobittencourt.bittencourt_academy.exception.exceptionsTypes;

public class AuthenticationProviderNotFoundException extends RuntimeException {
    public AuthenticationProviderNotFoundException() {
        super("Authentication provider not found");
    }
}
