package dev.pedrobittencourt.bittencourt_academy.exception;

public class ExpiredTokenException extends RuntimeException {
    public ExpiredTokenException(String message) {super(message);}
}
