package dev.pedrobittencourt.email_service.email;

import dev.pedrobittencourt.email_service.email.handler.EmailHandler;
import dev.pedrobittencourt.email_service.exception.InvalidEmailTypeException;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class EmailConsumer {
    private final List<EmailHandler> emailHandlers;

    @RabbitListener(queues = {"email.queue"})
    public void receive(EmailMessage message) {
        emailHandlers.stream()
                .filter(emailHandler -> emailHandler.type().equals(message.type()))
                .findFirst()
                .orElseThrow(() -> new InvalidEmailTypeException(message.type()))
                .handle(message);

        // Testar para quando enviar um tipo inexiste
        // Testar para quando não enviar os campos necessários
    }
}

