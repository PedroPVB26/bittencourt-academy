package dev.pedrobittencourt.bittencourt_academy.exception;

import dev.pedrobittencourt.bittencourt_academy.exception.exceptionsTypes.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<StandardError> handleUserNotFoundException(
            UserNotFoundException ex,
            HttpServletRequest request) {

        StandardError error = new StandardError(
                Instant.now(),
                HttpStatus.NOT_FOUND.value(),
                "USER_NOT_FOUND",
                ex.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<StandardError>  handleInvalidTokenException(InvalidTokenException ex,  HttpServletRequest request) {
        StandardError error = new StandardError(
                Instant.now(),
                HttpStatus.BAD_REQUEST.value(),
                "TOKEN_INVALID",
                ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(ExpiredTokenException.class)
    public ResponseEntity<StandardError>  handleExpiredTokenException(ExpiredTokenException ex,  HttpServletRequest request) {
        StandardError error = new StandardError(
                Instant.now(),
                HttpStatus.UNAUTHORIZED.value(),
                "TOKEN_EXPIRED",
                ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    @ExceptionHandler(EmailAlreadyInUseException.class)
    public ResponseEntity<StandardError>  handleEmailAlreadyInUseException(EmailAlreadyInUseException ex,  HttpServletRequest request) {
        StandardError error = new StandardError(
                Instant.now(),
                HttpStatus.CONFLICT.value(),
                "EMAIL_ALREADY_IN_USE",
                ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(EmailAlreadyVerifiedException.class)
    public ResponseEntity<StandardError> handleEmailAlreadyVerifiedException( EmailAlreadyVerifiedException ex, HttpServletRequest request) {
        StandardError error = new StandardError(
                Instant.now(),
                HttpStatus.BAD_REQUEST.value(),
                "EMAIL_ALREADY_VERIFIED",
                ex.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
}
