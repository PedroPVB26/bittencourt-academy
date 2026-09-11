package dev.pedrobittencourt.email_service.email.handler;

import dev.pedrobittencourt.email_service.email.EmailMessage;

public interface EmailHandler {
    String type();
    void handle(EmailMessage message);
}