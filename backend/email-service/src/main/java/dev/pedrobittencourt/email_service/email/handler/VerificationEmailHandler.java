package dev.pedrobittencourt.email_service.email.handler;

import dev.pedrobittencourt.email_service.email.EmailMessage;
import dev.pedrobittencourt.email_service.email.EmailService;
import dev.pedrobittencourt.email_service.exception.RequiredFieldNullException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Objects;

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
        String link = getRequiredField(message, "link");
        String userName = getRequiredField(message, "userName");
        emailService.sendVerificationEmail(message.destination(), link, userName);
    }

    private String getRequiredField(EmailMessage message, String fieldName) {
        Object value = message.data().get(fieldName);

        if (Objects.isNull(value)) {
            throw new RequiredFieldNullException(type(), fieldName);
        }

        return value.toString();
    }
}