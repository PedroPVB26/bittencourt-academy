package dev.pedrobittencourt.email_service.email;

public final class RabbitMQConstants {
    private RabbitMQConstants() {}

    public static final class Queues {
        public static final String EMAIL_VERIFICATION = "email.verification.queue";
        public static final String EMAIL_WELCOME = "email.welcome.queue";
        private Queues() {}
    }
}
