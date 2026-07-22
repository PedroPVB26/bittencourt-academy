package dev.pedrobittencourt.bittencourt_academy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Encoders;

import javax.crypto.SecretKey;

@SpringBootApplication
@ConfigurationPropertiesScan
public class BittencourtAcademyApplication {

	public static void main(String[] args) {
		SpringApplication.run(BittencourtAcademyApplication.class, args);
	}

}
