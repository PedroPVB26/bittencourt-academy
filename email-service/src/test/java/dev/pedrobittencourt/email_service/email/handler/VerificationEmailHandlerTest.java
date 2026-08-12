package dev.pedrobittencourt.email_service.email.handler;

import dev.pedrobittencourt.email_service.email.EmailMessage;
import dev.pedrobittencourt.email_service.email.EmailService;
import dev.pedrobittencourt.email_service.exception.RequiredFieldNullException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class VerificationEmailHandlerTest {
    @Mock
    private EmailService emailService;

    @InjectMocks
    private VerificationEmailHandler handler;


    @Test
    void shouldCallEmailService() {
        EmailMessage message = new EmailMessage(
                "EMAIL_VERIFICATION",
                "pedro@email.com",
                Map.of(
                        "link", "http://localhost/verify",
                        "userName", "Pedro"
                )
        );

        handler.handle(message);

        verify(emailService).sendVerificationEmail(
                "pedro@email.com",
                "http://localhost/verify",
                "Pedro"
        );
    }

    @Test
    void shouldThrowWhenLinkIsMissing() {

        EmailMessage message = new EmailMessage(
                "EMAIL_VERIFICATION",
                "pedro@email.com",
                Map.of(
                        "userName",
                        "Pedro"
                )
        );

        assertThrows(
                RequiredFieldNullException.class,
                () -> handler.handle(message)
        );
    }
}