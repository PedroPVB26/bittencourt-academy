package dev.pedrobittencourt.email_service.email;

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
}