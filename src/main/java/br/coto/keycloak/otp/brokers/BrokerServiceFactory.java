package br.coto.keycloak.otp.brokers;

import br.coto.keycloak.otp.brokers.mock.SimulateBrokerService;
import br.coto.keycloak.otp.brokers.zenvia.ZenviaBrokerService;
import br.coto.keycloak.otp.helpers.Brokers;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class BrokerServiceFactory {

    public static IBrokerService get(String brokerName, BrokerConfig config) throws Exception {
        log.debug("[COTO] Getting broker service for: {}", brokerName);

        switch (brokerName.toLowerCase()) {
            case "zenvia":
                return new ZenviaBrokerService( config );
            case "twilio":
                throw new Exception("[COTO] Twilio broker not implemented yet.");
            case "simulate":
                return new SimulateBrokerService( config );
            default:
                throw new Exception("[COTO] Unsupported broker: " + brokerName);
        }
    }

}
