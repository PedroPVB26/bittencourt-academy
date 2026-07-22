package dev.pedrobittencourt.bittencourt_academy.exception;

import dev.pedrobittencourt.bittencourt_academy.exception.exceptionsTypes.*;
import dev.pedrobittencourt.bittencourt_academy.exception.response.StandardError;
import dev.pedrobittencourt.bittencourt_academy.exception.response.ValidationError;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<StandardError> handleUserNotFoundException(
            UserNotFoundException ex,
            HttpServletRequest request
    ) {
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
    public ResponseEntity<StandardError>  handleInvalidTokenException(
            InvalidTokenException ex,
            HttpServletRequest request
    ) {
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
    public ResponseEntity<StandardError>  handleExpiredTokenException(
            ExpiredTokenException ex,
            HttpServletRequest request
    ) {
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
    public ResponseEntity<StandardError>  handleEmailAlreadyInUseException(
            EmailAlreadyInUseException ex,
            HttpServletRequest request
    ) {
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
    public ResponseEntity<StandardError> handleEmailAlreadyVerifiedException(
            EmailAlreadyVerifiedException ex,
            HttpServletRequest request
    ) {
        StandardError error = new StandardError(
                Instant.now(),
                HttpStatus.BAD_REQUEST.value(),
                "EMAIL_ALREADY_VERIFIED",
                ex.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<StandardError> handleHttpMessageNotReadableException(
            HttpMessageNotReadableException ex,
            HttpServletRequest request
    ) {
        StandardError error = new StandardError(
                Instant.now(),
                HttpStatus.BAD_REQUEST.value(),
                "INVALID_REQUEST_BODY",
                "Request body is invalid or malformed",
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationError> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException ex,
            HttpServletRequest request
    ) {

        Map<String, List<String>> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .collect(Collectors.groupingBy(
                        FieldError::getField,
                        Collectors.mapping(
                                FieldError::getDefaultMessage,
                                Collectors.toList()
                        )
                ));

        ValidationError error = new ValidationError(
                Instant.now(),
                HttpStatus.BAD_REQUEST.value(),
                "VALIDATION_ERROR",
                "One or more fields are invalid",
                request.getRequestURI(),
                errors
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<StandardError> handleHttpMediaTypeNotSupportedException(
            HttpMediaTypeNotSupportedException ex,
            HttpServletRequest request
    ) {

        StandardError error = new StandardError(
                Instant.now(),
                HttpStatus.UNSUPPORTED_MEDIA_TYPE.value(),
                "MEDIA_TYPE_NOT_SUPPORTED",
                "Content-Type is not supported. Use application/json",
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE).body(error);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<StandardError> handleMissingServletRequestParameterException(
            MissingServletRequestParameterException ex,
            HttpServletRequest request
    ) {

        StandardError error = new StandardError(
                Instant.now(),
                HttpStatus.BAD_REQUEST.value(),
                "MISSING_REQUEST_PARAMETER",
                String.format(
                        "Required parameter '%s' is missing",
                        ex.getParameterName()
                ),
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<StandardError> handleInvalidCredentialsException(
            InvalidCredentialsException ex,
            HttpServletRequest request
    ) {

        StandardError error = new StandardError(
                Instant.now(),
                HttpStatus.UNAUTHORIZED.value(),
                "INVALID_CREDENTIALS",
                ex.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<StandardError> handleDisabledException(
            DisabledException ex,
            HttpServletRequest request
    ) {

        StandardError error = new StandardError(
                Instant.now(),
                HttpStatus.FORBIDDEN.value(),
                "ACCOUNT_DISABLED",
                ex.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }

    @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
    public ResponseEntity<StandardError> accessDenied(
            org.springframework.security.access.AccessDeniedException e,
            HttpServletRequest request) {

        StandardError error = new StandardError(
                Instant.now(),
                HttpStatus.FORBIDDEN.value(),
                "ACCESS_DENIED",
                e.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(error);
    }


    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<StandardError> handleNoResourceFoundException(
            NoResourceFoundException ex,
            HttpServletRequest request
    ) {

        StandardError error = new StandardError(
                Instant.now(),
                HttpStatus.NOT_FOUND.value(),
                "RESOURCE_NOT_FOUND",
                "Endpoint not found",
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<StandardError> handleHttpRequestMethodNotSupportedException(
            HttpRequestMethodNotSupportedException ex,
            HttpServletRequest request
    ) {

        StandardError error = new StandardError(
                Instant.now(),
                HttpStatus.METHOD_NOT_ALLOWED.value(),
                "METHOD_NOT_ALLOWED",
                String.format(
                        "Method '%s' is not allowed for this endpoint",
                        ex.getMethod()
                ),
                request.getRequestURI()
        );

        return ResponseEntity
                .status(HttpStatus.METHOD_NOT_ALLOWED)
                .body(error);
    }
}
