package dev.pedrobittencourt.bittencourt_academy;

import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@EnableRabbit
@SpringBootApplication
@ConfigurationPropertiesScan
public class BittencourtAcademyApplication {
	public static void main(String[] args) {
		SpringApplication.run(BittencourtAcademyApplication.class, args);
	}
}
