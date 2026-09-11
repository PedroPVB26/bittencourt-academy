package dev.pedrobittencourt.bittencourt_academy.exception.exceptionsTypes;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(String message) {
        super("User not found." + message);
    }
}