package dev.pedrobittencourt.bittencourt_academy.exception.exceptionsTypes;

public class InvalidCredentialsException extends RuntimeException{
    public InvalidCredentialsException() {
        super("Incorrect email or password");
    }
}
