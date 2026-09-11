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
        publish(new EmailMessage(
                "EMAIL_VERIFICATION",
                destination,
                Map.of(
                        "userName", userName,
                        "link", link
                )
        ));
    }

    private void publish(EmailMessage message) {
        rabbitTemplate.convertAndSend(
                "email.exchange",
                "email.verification",
                message
        );
    }
}
