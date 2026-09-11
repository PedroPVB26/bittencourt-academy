package dev.pedrobittencourt.email_service.exception;

public class RequiredFieldNullException extends RuntimeException{
    public RequiredFieldNullException(String emailType, String fieldName) {
        super("Field '" + fieldName + "' is required for email type '" + emailType + "'");
    }
}
