package br.coto.keycloak.otp.brokers;

public interface IBrokerService {
    void send(String to, String message) throws Exception;
}
