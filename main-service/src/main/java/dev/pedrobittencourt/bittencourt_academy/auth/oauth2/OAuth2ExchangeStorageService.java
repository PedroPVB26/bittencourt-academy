package dev.pedrobittencourt.bittencourt_academy.auth.oauth2;

import dev.pedrobittencourt.bittencourt_academy.auth.model.OAuth2TokenExchange;
import dev.pedrobittencourt.bittencourt_academy.exception.exceptionsTypes.ExpiredTokenException;
import dev.pedrobittencourt.bittencourt_academy.exception.exceptionsTypes.InvalidTokenException;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class OAuth2ExchangeStorageService {
    // Futuramente posso utilzar o Redis?
    private final Map<String, OAuth2TokenExchange> exchangeCodes =  new ConcurrentHashMap<>();

    public void save(String code, OAuth2TokenExchange exchange) {
        exchangeCodes.put(code, exchange);
    }

    public OAuth2TokenExchange consume(String code) {
        OAuth2TokenExchange exchange = exchangeCodes.get(code);

        if(exchange == null){
            throw new InvalidTokenException("Invalid exchange code");
        }

        if(exchange.expiresAt().isBefore(Instant.now())){
            exchangeCodes.remove(code);
            throw new ExpiredTokenException("Expired exchange code");
        }

        return exchangeCodes.remove(code);
    }
}
