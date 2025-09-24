package br.coto.keycloak.otp;

import java.util.Arrays;
import java.util.List;

import br.coto.keycloak.otp.helpers.Brokers;
import br.coto.keycloak.otp.helpers.SMSFields;
import org.keycloak.Config;
import org.keycloak.authentication.Authenticator;
import org.keycloak.authentication.AuthenticatorFactory;
import org.keycloak.models.AuthenticationExecutionModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.provider.ProviderConfigProperty;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SmsAuthenticatorFactory implements AuthenticatorFactory {

    public static final String PROVIDER_ID = "sms-authenticator";

    private static final SmsAuthenticator SINGLETON = new SmsAuthenticator();

    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    @Override
    public String getDisplayType() {
        return "SMS Authentication";
    }

    @Override
    public String getHelpText() {
        return "Validates an OTP sent via SMS to the users mobile phone.";
    }

    @Override
    public String getReferenceCategory() {
        return "otp";
    }

    @Override
    public boolean isConfigurable() {
        return true;
    }

    @Override
    public boolean isUserSetupAllowed() {
        return true;
    }

    @Override
    public AuthenticationExecutionModel.Requirement[] getRequirementChoices() {
        return REQUIREMENT_CHOICES;
    }

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        String[] brokers = Arrays.stream(Brokers.values())
                                .map(Brokers::getDisplayName)
                                .toArray(String[]::new);

        return List.of(
                new ProviderConfigProperty(SMSFields.CODE_LENGTH.getValue(), "Code length", "The number of digits of the generated code.", ProviderConfigProperty.NUMBER_TYPE, 6),
                new ProviderConfigProperty(SMSFields.CODE_TTL.getValue(), "Time-to-live", "The time to live in seconds for the code to be valid.", ProviderConfigProperty.NUMBER_TYPE, "300"),
                new ProviderConfigProperty(SMSFields.SENDER_NAME.getValue(), "Sender Name", "The sender nema is displayed as the message sender on the receiving device.", ProviderConfigProperty.STRING_TYPE, "Keycloak"),
                new ProviderConfigProperty(SMSFields.SIMULATION_MODE.getValue(), "Simulation mode", "In simulation mode, the SMS won't be sent, but printed to the server logs", ProviderConfigProperty.BOOLEAN_TYPE, true),
                new ProviderConfigProperty(SMSFields.PHONE_ATTRIBUTE.getValue(), "Phone Attribute Name", "Define the name of the attribute to get the phone number", ProviderConfigProperty.USER_PROFILE_ATTRIBUTE_LIST_TYPE, null ),
                new ProviderConfigProperty(SMSFields.BROKER_LIST.getValue(), "Broker List", "List of supported brokers", ProviderConfigProperty.LIST_TYPE, null, brokers ),
                new ProviderConfigProperty(SMSFields.BROKER_KEY.getValue(), "Broker Key/User", "The username or api key", ProviderConfigProperty.STRING_TYPE, null ),
                new ProviderConfigProperty(SMSFields.BROKER_SECRET.getValue(), "Broker Secret/Pass", "The password or api key", ProviderConfigProperty.PASSWORD, null ),
                new ProviderConfigProperty(SMSFields.BROKER_SHORT_CODE.getValue(), "Broker ShortCode/From Number", "The sender number", ProviderConfigProperty.STRING_TYPE, null )
        );
    }

    @Override
    public Authenticator create(KeycloakSession session) {
        return SINGLETON;
    }

    @Override
    public void init(Config.Scope config) {
    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {
    }

    @Override
    public void close() {
    }

}