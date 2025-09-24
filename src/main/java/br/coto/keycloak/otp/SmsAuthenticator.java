package br.coto.keycloak.otp;

import br.coto.keycloak.otp.brokers.BrokerConfig;
import br.coto.keycloak.otp.brokers.BrokerServiceFactory;
import br.coto.keycloak.otp.helpers.SMSFields;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.AuthenticationFlowError;
import org.keycloak.authentication.Authenticator;
import org.keycloak.common.util.SecretGenerator;
import org.keycloak.models.AuthenticationExecutionModel;
import org.keycloak.models.AuthenticatorConfigModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.sessions.AuthenticationSessionModel;
import org.keycloak.theme.Theme;

import java.util.Locale;

/**
 * An authenticator that sends a one-time password (OTP) via SMS to the user's registered mobile number.
 * The user is then prompted to enter the received OTP for verification.
 * <p>
 * Configuration options include:
 * <ul>
 *     <li>CODE_LENGTH: Length of the OTP code (default is 6 digits).</li>
 *     <li>CODE_TTL: Time-to-live for the OTP code in seconds (default is 300 seconds).</li>
 *     <li>SMS_PROVIDER: The SMS service provider to use for sending messages.</li>
 *     <li>Other provider-specific configuration parameters.</li>
 * </ul>
 * <p>
 * The authenticator checks if the user has a registered mobile number and sends the OTP to that number.
 * It handles user input, validates the OTP, and manages expiration and error scenarios.
 */
@Slf4j
public class SmsAuthenticator implements Authenticator {

    private static final String PHONE_NUMBER = "mobile_number";
    private static final String TPL_CODE = "login-otp-sms.ftl";

    @Override
    public void authenticate(AuthenticationFlowContext context) {
        log.debug("[COTO] SmsAuthenticator authenticate");
        AuthenticatorConfigModel config = context.getAuthenticatorConfig();
        KeycloakSession session = context.getSession();

        int length = Integer.parseInt(config.getConfig().get(SMSFields.CODE_LENGTH.getValue()));
        int ttl = Integer.parseInt(config.getConfig().get(SMSFields.CODE_TTL.getValue()));
        String phoneAttributeName = config.getConfig().get( SMSFields.PHONE_ATTRIBUTE.getValue() );
        String broker = config.getConfig().get( SMSFields.BROKER_LIST.getValue() );

        if (Boolean.parseBoolean(config.getConfig().getOrDefault(SMSFields.SIMULATION_MODE.getValue(), "false"))) {
            broker = "simulate";
        }

        log.debug("[COTO] Phone attribute name: {}", phoneAttributeName);

        UserModel user = context.getUser();
        String mobileNumber = user.getFirstAttribute(phoneAttributeName != null ? phoneAttributeName : PHONE_NUMBER);

        // Mask phone number for logging (security improvement)
        log.debug("[COTO] User mobile number configured: {}", mobileNumber != null ? maskPhoneNumber(mobileNumber) : "none");

        if (mobileNumber == null || mobileNumber.trim().isEmpty()) {
            log.warn("[COTO] User has no phone number configured: {}", user.getUsername());
            context.failureChallenge(AuthenticationFlowError.CLIENT_CREDENTIALS_SETUP_REQUIRED,
                    context.form().setError("smsAuthMobileNumberMissing")
                            .createErrorPage(Response.Status.BAD_REQUEST));
            return;
        }

        // Validate phone number format (basic validation)
        if (!isValidPhoneNumber(mobileNumber)) {
            log.warn("[COTO] Invalid phone number format for user: {}", user.getUsername());
            context.failureChallenge(AuthenticationFlowError.CLIENT_CREDENTIALS_SETUP_REQUIRED,
                    context.form().setError("smsAuthMobileNumberInvalid")
                            .createErrorPage(Response.Status.BAD_REQUEST));
            return;
        }

        String code = SecretGenerator.getInstance().randomString(length, SecretGenerator.DIGITS);
        AuthenticationSessionModel authSession = context.getAuthenticationSession();
        authSession.setAuthNote(SMSFields.CODE.getValue(), code);
        authSession.setAuthNote(SMSFields.CODE_TTL.getValue(), Long.toString(System.currentTimeMillis() + (ttl * 1000L)));

        try {
            Theme theme = session.theme().getTheme(Theme.Type.LOGIN);
            Locale locale = session.getContext().resolveLocale(user);

            String smsAuthText = theme.getMessages(locale).getProperty("smsAuthText");
            String realmName = context.getRealm().getDisplayName() != null ? context.getRealm().getDisplayName() : context.getRealm().getName();
            String smsText = String.format(smsAuthText, code, realmName);

            BrokerConfig brokerConfig = BrokerConfig.builder()
                                            .shortCode(config.getConfig().get(SMSFields.BROKER_SHORT_CODE.getValue()))
                                            .key(config.getConfig().get(SMSFields.BROKER_KEY.getValue()))
                                            .secret(config.getConfig().get(SMSFields.BROKER_SECRET.getValue()))
                                            .build();

            BrokerServiceFactory.get(broker, brokerConfig).send(mobileNumber, smsText);

            log.info("[COTO] SMS OTP sent successfully to user: {} (phone: {})", user.getUsername(), maskPhoneNumber(mobileNumber));

            context.challenge(context.form()
                    .setAttribute("realm", context.getRealm())
                    .createForm(TPL_CODE)
            );
        } catch (Exception e) {
            log.error("[COTO] Failed to send SMS OTP to user: {} - Error: {}", user.getUsername(), e.getMessage());
            context.failureChallenge(AuthenticationFlowError.INTERNAL_ERROR,
                    context.form().setError("smsAuthSmsNotSent", e.getMessage())
                            .createErrorPage(Response.Status.INTERNAL_SERVER_ERROR));
        }
    }

    @Override
    public void action(AuthenticationFlowContext context) {
        log.debug("[COTO] SmsAuthenticator action");
        String enteredCode = context.getHttpRequest().getDecodedFormParameters().getFirst(SMSFields.CODE.getValue());

        // Validate entered code format
        if (enteredCode == null || enteredCode.trim().isEmpty() || !enteredCode.matches("\\d+")) {
            log.warn("[COTO] Invalid code format entered by user: {}", context.getUser().getUsername());
            AuthenticationExecutionModel execution = context.getExecution();
            if (execution.isRequired()) {
                context.failureChallenge(AuthenticationFlowError.INVALID_CREDENTIALS,
                        context.form().setAttribute("realm", context.getRealm())
                                .setError("smsAuthCodeInvalid").createForm(TPL_CODE));
            } else if (execution.isConditional() || execution.isAlternative()) {
                context.attempted();
            }
            return;
        }

        AuthenticationSessionModel authSession = context.getAuthenticationSession();
        String code = authSession.getAuthNote(SMSFields.CODE.getValue());
        String ttl = authSession.getAuthNote(SMSFields.CODE_TTL.getValue());

        if (code == null || ttl == null) {
            log.error("[COTO] Missing OTP code or TTL in session for user: {}", context.getUser().getUsername());
            context.failureChallenge(AuthenticationFlowError.INTERNAL_ERROR,
                    context.form().createErrorPage(Response.Status.INTERNAL_SERVER_ERROR));
            return;
        }

        boolean isValid = enteredCode.equals(code);
        if (isValid) {
            if (Long.parseLong(ttl) < System.currentTimeMillis()) {
                log.warn("[COTO] Expired OTP code used by user: {}", context.getUser().getUsername());
                context.failureChallenge(AuthenticationFlowError.EXPIRED_CODE,
                        context.form().setError("smsAuthCodeExpired").createErrorPage(Response.Status.BAD_REQUEST));
            } else {
                log.info("[COTO] SMS OTP authentication successful for user: {}", context.getUser().getUsername());
                // Clean up the OTP from session for security
                authSession.removeAuthNote(SMSFields.CODE.getValue());
                authSession.removeAuthNote(SMSFields.CODE_TTL.getValue());
                context.success();
            }
        } else {
            log.warn("[COTO] Invalid OTP code entered by user: {}", context.getUser().getUsername());
            AuthenticationExecutionModel execution = context.getExecution();
            if (execution.isRequired()) {
                context.failureChallenge(AuthenticationFlowError.INVALID_CREDENTIALS,
                        context.form().setAttribute("realm", context.getRealm())
                                .setError("smsAuthCodeInvalid").createForm(TPL_CODE));
            } else if (execution.isConditional() || execution.isAlternative()) {
                context.attempted();
            }
        }
    }

    @Override
    public boolean requiresUser() {
        log.debug("[COTO] SmsAuthenticator requires user autenticated");
        return true;
    }

    @Override
    public boolean configuredFor(KeycloakSession session, RealmModel realm, UserModel user) {
        log.debug("[COTO] Checking if user is configured for SMS OTP: {}", user.getUsername());
//        return user.getFirstAttribute(PHONE_NUMBER) != null;
        return true;
    }

    @Override
    public void setRequiredActions(KeycloakSession session, RealmModel realm, UserModel user) {
        log.debug("[COTO] Setting required action for user to configure phone number: {}", user.getUsername());
    }

    @Override
    public void close() {
        log.debug("[COTO] Closing SmsAuthenticator");
    }

    /**
     * Masks phone number for logging purposes (security)
     */
    private String maskPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.length() <= 4) {
            return "****";
        }
        int visibleChars = Math.min(4, phoneNumber.length() / 3);
        StringBuilder masked = new StringBuilder();
        masked.append(phoneNumber.substring(0, visibleChars));
        for (int i = visibleChars; i < phoneNumber.length() - visibleChars; i++) {
            masked.append("*");
        }
        if (phoneNumber.length() > visibleChars) {
            masked.append(phoneNumber.substring(phoneNumber.length() - visibleChars));
        }
        return masked.toString();
    }

    /**
     * Basic phone number validation
     */
    private boolean isValidPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            return false;
        }
        // Remove espaços e hífens
        String cleanNumber = phoneNumber.replaceAll("[\\s\\-]", "");
        // Valida formato internacional: começa com + seguido de 8 a 15 dígitos
        return cleanNumber.matches("^\\+[1-9]\\d{7,14}$");
    }
}