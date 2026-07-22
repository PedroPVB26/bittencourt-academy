package dev.pedrobittencourt.email_service.email.handler;

import dev.pedrobittencourt.email_service.email.EmailMessage;
import dev.pedrobittencourt.email_service.email.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class VerificationEmailHandler implements EmailHandler {
    private final EmailService emailService;

    @Override
    public String type() {
        return "EMAIL_VERIFICATION";
    }

    @Override
    public void handle(EmailMessage message) {
        emailService.sendVerificationEmail(
            message.destination(),
            (String) message.data().get("link"),
            (String) message.data().get("userName")
        );
    }
}