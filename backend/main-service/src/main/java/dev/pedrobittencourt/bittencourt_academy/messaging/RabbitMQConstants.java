package dev.pedrobittencourt.bittencourt_academy.messaging;

public final class RabbitMQConstants {
    private RabbitMQConstants() {}

    public static final String EMAIL_EXCHANGE = "email.exchange";

    public static final class RoutingKeys {
        public static final String EMAIL_VERIFICATION = "email.verification";
        public static final String EMAIL_WELCOME = "email.welcome";
        private RoutingKeys() {}
    }
}
