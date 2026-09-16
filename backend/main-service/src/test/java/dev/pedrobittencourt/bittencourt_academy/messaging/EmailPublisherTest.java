package dev.pedrobittencourt.bittencourt_academy.messaging;

import dev.pedrobittencourt.bittencourt_academy.dto.EmailMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class EmailPublisherTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private EmailPublisher emailPublisher;

    @Test
    void shouldSendVerificationEmail() {
        emailPublisher.sendVerificationEmail(
                "pedro@email.com",
                "http://localhost:8080/auth/verify-email",
                "Pedro"
        );

        ArgumentCaptor<EmailMessage> messageCaptor = ArgumentCaptor.forClass(EmailMessage.class);

        verify(rabbitTemplate).convertAndSend(
                eq(RabbitMQConstants.EMAIL_EXCHANGE),
                eq(RabbitMQConstants.RoutingKeys.EMAIL_VERIFICATION),
                messageCaptor.capture()
        );

        EmailMessage message = messageCaptor.getValue();
        assertEquals("pedro@email.com", message.destination());
        assertEquals("Pedro", message.data().get("userName"));
        assertEquals("http://localhost:8080/auth/verify-email", message.data().get("link")
        );
    }

    @Test
    void shouldSendWelcomeEmail() {
        emailPublisher.sendWelcomeEmail("pedro@email.com", "Pedro Paulo");

        ArgumentCaptor<EmailMessage> messageCaptor = ArgumentCaptor.forClass(EmailMessage.class);

        verify(rabbitTemplate).convertAndSend(
                eq(RabbitMQConstants.EMAIL_EXCHANGE),
                eq(RabbitMQConstants.RoutingKeys.EMAIL_WELCOME),
                messageCaptor.capture()
        );

        EmailMessage message = messageCaptor.getValue();
        assertEquals("pedro@email.com", message.destination());
        assertEquals("Pedro Paulo", message.data().get("userName"));
    }
}