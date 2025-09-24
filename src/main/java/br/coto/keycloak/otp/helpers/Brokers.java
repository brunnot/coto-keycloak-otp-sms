package br.coto.keycloak.otp.helpers;

public enum Brokers {

    ZENVIA("zenvia", "Zenvia"),
    TWILIO("twilio", "Twilio");

    private final String value;
    private final String displayName;

    Brokers(String value, String displayName) {
        this.value = value;
        this.displayName = displayName;
    }

    public String getValue() {
        return value;
    }

    public String getDisplayName() {
        return displayName;
    }
}


