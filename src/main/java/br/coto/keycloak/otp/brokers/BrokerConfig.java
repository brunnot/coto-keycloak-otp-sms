package br.coto.keycloak.otp.brokers;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class BrokerConfig {
    private String shortCode;
    private String key;
    private String secret;
}
