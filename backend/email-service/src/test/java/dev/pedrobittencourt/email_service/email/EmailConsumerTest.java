package dev.pedrobittencourt.email_service.email;

import dev.pedrobittencourt.email_service.email.handler.EmailHandler;
import dev.pedrobittencourt.email_service.exception.InvalidEmailTypeException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmailConsumerTest {
    @Mock
    private EmailHandler verificationHandler;

    @InjectMocks
    private EmailConsumer consumer;

    @BeforeEach
    void setUp() {
        consumer = new EmailConsumer(
                List.of(verificationHandler)
        );
    }

    @Test
    void shouldCallCorrectHandler() {
        EmailMessage message = new EmailMessage(
                "EMAIL_VERIFICATION",
                "pedro@email.com",
                Map.of()
        );

        when(verificationHandler.type()).thenReturn("EMAIL_VERIFICATION");
        consumer.receive(message);
        verify(verificationHandler).handle(message);
    }

    @Test
    void shouldThrowWhenHandlerDoesNotExist() {
        EmailMessage message = new EmailMessage(
                "INVALID_TYPE",
                "pedro@email.com",
                Map.of()
        );

        when(verificationHandler.type()).thenReturn("EMAIL_VERIFICATION");

        assertThrows(
                InvalidEmailTypeException.class,
                () -> consumer.receive(message)
        );
    }
}