package br.coto.keycloak.otp.brokers.zenvia;

import br.coto.keycloak.otp.brokers.BrokerConfig;
import br.coto.keycloak.otp.brokers.IBrokerService;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;

@Slf4j
public class ZenviaBrokerService implements IBrokerService {

    private static final String ZENVIA_API_URL = "https://api-rest.zenvia.com/services/send-sms";

    private final BrokerConfig config;
    private final HttpClient httpClient;

    public ZenviaBrokerService(BrokerConfig config) {
        this.validate(config);
        this.config = config;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(30))
                .build();
    }

    private void validate(BrokerConfig config) {
        if (config == null) {
            throw new IllegalArgumentException("[COTO] ZenviaBrokerService: config cannot be null");
        }
        if (config.getKey() == null || config.getKey().isEmpty()) {
            throw new IllegalArgumentException("[COTO] ZenviaBrokerService: key cannot be null or empty");
        }
        if (config.getSecret() == null || config.getSecret().isEmpty()) {
            throw new IllegalArgumentException("[COTO] ZenviaBrokerService: secret cannot be null or empty");
        }
        if (config.getShortCode() == null || config.getShortCode().isEmpty()) {
            throw new IllegalArgumentException("[COTO] ZenviaBrokerService: shortCode cannot be null or empty");
        }
    }

    @Override
    public void send(String to, String message) throws Exception {
        log.debug("[COTO] Send using Zenvia: Data: to: {} / message: {}", to, message);
        try {
            String jsonPayload = buildJsonPayload(to, message);
            String authHeader = "Basic " + Base64.getEncoder()
                    .encodeToString((config.getKey() + ":" + config.getSecret()).getBytes(StandardCharsets.UTF_8));

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(ZENVIA_API_URL))
                    .header("Authorization", authHeader)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonPayload, StandardCharsets.UTF_8))
                    .timeout(Duration.ofSeconds(30))
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                log.info("[COTO] SMS sent successfully via Zenvia to: {}", to);
            } else {
                log.error("[COTO] Zenvia SMS send failed. Status: {} - Body: {}", response.statusCode(), response.body());
                throw new Exception("Failed to send SMS via Zenvia. HTTP Status: " + response.statusCode());
            }
        } catch (IOException | InterruptedException e) {
            log.error("[COTO] Error sending SMS via Zenvia: {}", e.getMessage(), e);
            throw new Exception("Failed to send SMS via Zenvia", e);
        }
    }

    private String buildJsonPayload(String to, String message) {
        // Zenvia classic API payload
        return String.format(
                "{\"sendSmsRequest\":{\"to\":\"%s\",\"msg\":\"%s\",\"sender\":\"%s\"}}",
                to,
                message.replace("\"", "\\\""),
                config.getShortCode()
        );
    }
}
