package dev.pedrobittencourt.email_service.email;

import dev.pedrobittencourt.email_service.exception.RequiredFieldNullException;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private SpringTemplateEngine templateEngine;

    private EmailService emailService;

    @BeforeEach
    void setUp() {
        emailService = new EmailService(
                mailSender,
                "noreply@test.com",
                templateEngine
        );
    }

    @Test
    @DisplayName("Should send verification email successfully")
    void sendVerificationEmail() throws Exception {
        MimeMessage mimeMessage = new MimeMessage((Session) null);

        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        when(templateEngine.process(
                eq("emails/email-verification"),
                any(Context.class)
        )).thenReturn("<h1>Verification Email</h1>");

        emailService.sendVerificationEmail(
                "pedrovittencourt@gmail.com",
                "http://localhost:8080/verify",
                "Pedro"
        );

        verify(mailSender).send(mimeMessage);

        ArgumentCaptor<Context> contextCaptor = ArgumentCaptor.forClass(Context.class);

        verify(templateEngine).process(
                eq("emails/email-verification"),
                contextCaptor.capture()
        );

        Context context = contextCaptor.getValue();

        assertEquals(
                "Pedro",
                context.getVariable("userName")
        );

        assertEquals(
                "http://localhost:8080/verify",
                context.getVariable("link")
        );
    }

    @Test
    @DisplayName("Should send welcome email successfully")
    void sendWelcomeEmail() throws Exception {
        MimeMessage mimeMessage = new MimeMessage((Session) null);

        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        when(templateEngine.process(
                eq("emails/email-welcome"),
                any(Context.class)
        )).thenReturn("<h1>Welcome Email</h1>");

        emailService.sendWelcomeEmail(
                "pedrovittencourt@gmail.com",
                "Pedro"
        );

        verify(mailSender).send(mimeMessage);

        ArgumentCaptor<Context> contextCaptor =
                ArgumentCaptor.forClass(Context.class);

        verify(templateEngine).process(
                eq("emails/email-welcome"),
                contextCaptor.capture()
        );

        Context context = contextCaptor.getValue();

        assertEquals(
                "Pedro",
                context.getVariable("userName")
        );
    }

    @Test
    @DisplayName("Should return required field value")
    void getRequiredField() {
        EmailMessage message = new EmailMessage(
                "pedrovittencourt@gmail.com",
                Map.of(
                        "userName", "Pedro",
                        "link", "http://localhost:8080/verify"
                )
        );

        String result = EmailService.getRequiredField(
                "EMAIL_VERIFICATION",
                message,
                "userName"
        );

        assertEquals("Pedro", result);
    }

    @Test
    @DisplayName("Should throw exception when required field is null")
    void getRequiredFieldShouldThrowException() {
        EmailMessage message = new EmailMessage(
                "pedrovittencourt@gmail.com",
                Map.of()
        );

        RequiredFieldNullException exception = assertThrows(
                RequiredFieldNullException.class,
                () -> EmailService.getRequiredField(
                        "EMAIL_VERIFICATION",
                        message,
                        "userName"
                )
        );

        assertNotNull(exception);
    }
}