package dev.pedrobittencourt.bittencourt_academy.exception;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(String message) {
        super("User not found." + message);
    }
}