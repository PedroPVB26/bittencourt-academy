package dev.pedrobittencourt.bittencourt_academy.exception;

public class EmailAlreadyVerifiedException extends RuntimeException {
    public EmailAlreadyVerifiedException(String message) {super(message);}
}
