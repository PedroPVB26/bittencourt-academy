package dev.pedrobittencourt.email_service.email.handler;

import dev.pedrobittencourt.email_service.email.EmailMessage;
import dev.pedrobittencourt.email_service.email.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WelcomeEmailHandler implements EmailHandler  {
    private final EmailService emailService;

    @Override
    public String type() { return "EMAIL_WELCOME"; }

    @Override
    public void handle(EmailMessage message) {
        String userName = EmailService.getRequiredField(type(), message, "userName");
        emailService.sendWelcomeEmail(message.destination(), userName);
    }
}
