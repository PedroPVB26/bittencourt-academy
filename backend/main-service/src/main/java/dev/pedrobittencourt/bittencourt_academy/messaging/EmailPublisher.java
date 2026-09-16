package dev.pedrobittencourt.bittencourt_academy.messaging;

import dev.pedrobittencourt.bittencourt_academy.dto.EmailMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class EmailPublisher {
    private final RabbitTemplate rabbitTemplate;

    public void sendVerificationEmail(String destination, String link, String userName){
        publishEmail(
                new EmailMessage(
                    destination,
                    Map.of(
                            "userName", userName,
                            "link", link
                    )
                ),
                RabbitMQConstants.RoutingKeys.EMAIL_VERIFICATION
        );
    }

    public void sendWelcomeEmail(String destination, String userName){
        publishEmail(
                new EmailMessage(destination, Map.of("userName", userName)), RabbitMQConstants.RoutingKeys.EMAIL_WELCOME
        );
    }

    private void publishEmail(EmailMessage message, String routingKey) {
        rabbitTemplate.convertAndSend(
                RabbitMQConstants.EMAIL_EXCHANGE,
                routingKey,
                message
        );
    }
}
