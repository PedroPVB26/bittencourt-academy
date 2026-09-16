package dev.pedrobittencourt.email_service.email;

import dev.pedrobittencourt.email_service.email.handler.EmailHandler;
import dev.pedrobittencourt.email_service.email.handler.VerificationEmailHandler;
import dev.pedrobittencourt.email_service.email.handler.WelcomeEmailHandler;
import dev.pedrobittencourt.email_service.exception.InvalidEmailTypeException;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class EmailConsumer {
    private final VerificationEmailHandler verificationEmailHandler;
    private final WelcomeEmailHandler welcomeEmailHandler;

    @RabbitListener(queues = {RabbitMQConstants.Queues.EMAIL_VERIFICATION})
    public void receiveVerification(EmailMessage message) {
        verificationEmailHandler.handle(message);
    }

    @RabbitListener(queues = {RabbitMQConstants.Queues.EMAIL_WELCOME})
    public void receiveWelcome(EmailMessage message) {
        welcomeEmailHandler.handle(message);
    }
}

