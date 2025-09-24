package br.coto.keycloak.otp.helpers;

public enum SMSFields {
    CODE("code"),
    CODE_LENGTH("length"),
    CODE_TTL("ttl"),
    SENDER_NAME("senderName"),
    SIMULATION_MODE("simulation"),
    PHONE_ATTRIBUTE("phone_attribute_name"),
    BROKER_LIST("brokers"),
    BROKER_SHORT_CODE( "broker_short_code" ),
    BROKER_KEY( "broker_key" ),
    BROKER_SECRET( "broker_secret" );

    private final String value;

    SMSFields(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
