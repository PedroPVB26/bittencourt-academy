package dev.pedrobittencourt.email_service.email;

import com.icegreen.greenmail.configuration.GreenMailConfiguration;
import com.icegreen.greenmail.junit5.GreenMailExtension;
import com.icegreen.greenmail.util.ServerSetupTest;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class EmailServiceIntegrationTest {
    @Autowired
    private EmailService emailService;

    @RegisterExtension
    static GreenMailExtension greenMail = new GreenMailExtension(ServerSetupTest.SMTP)
            .withConfiguration(
                    GreenMailConfiguration.aConfig()
                            .withUser("test", "test")
            );

    @Test
    @DisplayName("Should send verification email")
    void shouldSendVerificationEmail() throws Exception {

        emailService.sendVerificationEmail(
                "pedro@localhost",
                "http://localhost:8080/verify",
                "Pedro"
        );

        assertTrue(greenMail.waitForIncomingEmail(5000, 1));

        MimeMessage[] receivedMessages = greenMail.getReceivedMessages();

        assertEquals(1, receivedMessages.length);

        MimeMessage email = receivedMessages[0];

        assertEquals("Welcome to Bittencourt Academy!", email.getSubject());

        MimeMultipart mixed = (MimeMultipart) email.getContent();

        MimeMultipart related = (MimeMultipart) mixed
                        .getBodyPart(0)
                        .getContent();

        String html = related
                .getBodyPart(0)
                .getContent()
                .toString();

        assertAll(
                () -> assertTrue(html.contains("Pedro")),
                () -> assertTrue(html.contains("Activate account")),
                () -> assertTrue(html.contains("http://localhost:8080/verify"))
        );
    }
}
