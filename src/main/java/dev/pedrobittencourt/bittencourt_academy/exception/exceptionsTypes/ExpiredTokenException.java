package dev.pedrobittencourt.bittencourt_academy.exception.exceptionsTypes;

public class ExpiredTokenException extends RuntimeException {
    public ExpiredTokenException(String message) {super(message);}
}
