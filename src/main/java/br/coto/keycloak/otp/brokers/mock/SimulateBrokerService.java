package br.coto.keycloak.otp.brokers.mock;

import br.coto.keycloak.otp.brokers.BrokerConfig;
import br.coto.keycloak.otp.brokers.IBrokerService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SimulateBrokerService implements IBrokerService {
    private final BrokerConfig config;

    public SimulateBrokerService(BrokerConfig config) {
        this.config = config;
    }

    @Override
    public void send(String to, String message) throws Exception {
        log.warn("[COTO] Simulate mode. Use just on development environment.");
        log.info("Simulating sending SMS to: {}", to);
        log.info("Authentication code: {}", message);
        log.info("BrokerConfig settings:");
        log.info("Key: {}", config.getKey());
        log.info("Secret: {}", maskSecret( config.getSecret()) );
        log.info("Shortcode: {}", config.getShortCode());
    }

    private String maskSecret(String secret) {
        if (secret == null || secret.length() <= 4) return secret;
        int maskLength = secret.length() - 4;
        StringBuilder masked = new StringBuilder();
        masked.append(secret.substring(0, 2));
        for (int i = 0; i < maskLength; i++) masked.append("*");
        masked.append(secret.substring(secret.length() - 2));
        return masked.toString();
    }
}
