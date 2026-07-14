package dev.pedrobittencourt.bittencourt_academy.email;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;


@Service
public class EmailService {
    private final JavaMailSender mailSender;
    private final String fromEmail;
    private final SpringTemplateEngine templateEngine;

    public EmailService(JavaMailSender mailSender, @Value("${spring.mail.username}")  String fromEmail, SpringTemplateEngine templateEngine) {
        this.mailSender = mailSender;
        this.fromEmail = fromEmail;
        this.templateEngine = templateEngine;
    }

    public void sendVerificationEmail(String destination, String link, String userName){
        try{
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true,  "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(destination);
            helper.setSubject("Welcome to Bittencourt Academy!");

            Context context = new Context();
            context.setVariable("userName", userName);
            context.setVariable("link", link);

            String html = templateEngine.process("emails/email-verification", context);

            helper.setText(html, true);

            mailSender.send(message);

        }catch(Exception e){
            e.printStackTrace();
            throw new RuntimeException("Error while sending verification email");
        }
    }
}
