# Keycloak SMS OTP Authenticator

[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://openjdk.java.net/projects/jdk/17/)
[![License](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)
[![Build Status](https://img.shields.io/badge/Build-Passing-brightgreen.svg)](#)

A custom Keycloak authenticator that provides SMS-based One-Time Password (OTP) authentication. This extension allows users to receive OTP codes via SMS as a second factor authentication method.

## 🚀 Features

- **SMS OTP Authentication**: Send one-time passwords via SMS to users' mobile phones
- **Multiple SMS Providers**: Support for different SMS service providers (Zenvia, Mock for testing)
- **Configurable Code Settings**: Customizable code length (4-10 digits) and expiration time (1 minute - 1 hour)
- **Internationalization**: Support for English and Portuguese (Brazil) languages
- **Security Features**: 
  - Phone number validation
  - Code expiration handling
  - Secure logging (masked phone numbers)
  - Input validation and sanitization
- **Simulation Mode**: Test mode for development without sending real SMS
- **Easy Configuration**: Admin UI configuration for all settings

## 📋 Requirements

- **Java**: 17 or higher
- **Keycloak**: >= 24.x.x (compatible with other recent versions)
- **Maven**: 3.6+ (for building from source)
- **SMS Provider**: Account with supported SMS service (Zenvia or custom implementation)

## 🛠️ Installation

### Option 1: Download Pre-built JAR

1. Download the latest `otp-sms-1.0.0.jar` from the [Releases](../../releases) page
2. Copy the JAR to your Keycloak providers directory:
   ```bash
   cp otp-sms-1.0.0.jar /opt/keycloak/providers/
   ```
3. Restart Keycloak or rebuild (for production mode):
   ```bash
   /opt/keycloak/bin/kc.sh build
   ```

### Option 2: Build from Source

1. Clone the repository:
   ```bash
   git clone https://github.com/your-username/coto-keycloak-otp-sms.git
   cd coto-keycloak-otp-sms
   ```

2. Build the project:
   ```bash
   mvn clean package
   ```

3. Copy the generated JAR:
   ```bash
   cp target/otp-sms-1.0.0.jar /opt/keycloak/providers/
   ```

4. Restart Keycloak

## ⚙️ Configuration

### 1. Authentication Flow Setup

1. Login to Keycloak Admin Console
2. Navigate to **Authentication** → **Flows**
3. Create a new flow or copy an existing one
4. Add **SMS Authentication** as an execution step
5. Configure the execution as **Required** or **Alternative**

### 2. SMS Authenticator Configuration

Configure the following settings in the authenticator configuration:

| Setting | Description | Default | Required |
|---------|-------------|---------|----------|
| **Code Length** | Number of digits in the OTP code (4-10) | 6 | No |
| **Time-to-live** | Code expiration time in seconds (60-3600) | 300 | No |
| **Simulation Mode** | Enable test mode (logs code instead of sending SMS) | false | No |
| **Phone Attribute Name** | User attribute containing phone number | mobile_number | No |
| **Broker List** | SMS service provider to use | - | Yes |
| **Broker Key/User** | SMS provider username or API key | - | Yes |
| **Broker Secret/Pass** | SMS provider password or secret | - | Yes |
| **Broker ShortCode/From** | Sender number or short code | - | Yes |

### 3. User Phone Number Setup

Users must have a phone number configured in their profile. The phone number should be stored in the user attribute specified in the **Phone Attribute Name** setting (default: `mobile_number`).

**Phone Number Format**: The authenticator accepts phone numbers in formats:
- International format: `+5511999999999`

## 📱 Supported SMS Providers

### Zenvia
Configure your Zenvia credentials:
- **Broker List**: Select "Zenvia"
- **Broker Key/User**: Your Zenvia username
- **Broker Secret/Pass**: Your Zenvia password
- **Broker ShortCode/From**: Your sender number or short code

### Mock/Simulation
For testing purposes:
- **Simulation Mode**: Enable this option
- The OTP code will be logged in Keycloak logs instead of sent via SMS

### Custom Providers
Implement the `IBrokerService` interface to add support for other SMS providers:

```java
public class CustomBrokerService implements IBrokerService {
    @Override
    public void send(String to, String message) throws Exception {
        // Your SMS sending implementation
    }
}
```

## 🐳 Docker Setup (Development)

Use the provided `docker-compose.yml` for local development:

```bash
# Build the project first
mvn clean package

# Start Keycloak with PostgreSQL
docker-compose up -d

# Access Keycloak at http://localhost:8080
# Admin credentials: admin/admin
```

## 🔧 Customization

### Custom Messages
Modify the message templates in:
- `src/main/resources/theme-resources/messages/messages_en.properties` (English)
- `src/main/resources/theme-resources/messages/messages_pt_BR.properties` (Portuguese)

### Custom UI Template
The login template is located at:
- `src/main/resources/theme-resources/templates/login-otp-sms.ftl`

## 🔐 Security Considerations

- **Phone Number Privacy**: Phone numbers are masked in logs for security
- **Code Validation**: Only numeric codes are accepted with proper length validation
- **Rate Limiting**: Consider implementing rate limiting at the SMS provider level
- **Code Cleanup**: OTP codes are automatically removed from session after use
- **Input Sanitization**: All user inputs are validated and sanitized
- **Secure Configuration**: Use strong credentials for SMS provider accounts

## 🧪 Testing

### Simulation Mode
Enable simulation mode for testing without sending real SMS messages:
1. Set **Simulation Mode** to `true` in authenticator configuration
2. Check Keycloak logs for the OTP codes
3. Use the logged codes for testing authentication

### Integration Testing
1. Configure a test SMS provider account
2. Create test users with valid phone numbers
3. Test the complete authentication flow
4. Verify code expiration and validation logic

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📝 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🆘 Support & Issues

- **Issues**: Report bugs and request features on [GitHub Issues](../../issues)
- **Discussions**: Join community discussions on [GitHub Discussions](../../discussions)
- **Documentation**: Check the [Wiki](../../wiki) for additional documentation

## 🏷️ Changelog

### v1.0.0
- Initial release
- Support for Zenvia SMS provider
- Configurable OTP codes and expiration
- Internationalization support (EN/PT-BR)
- Security improvements and input validation
- Simulation mode for testing

## 👥 Authors

- **Coto Team** - *Initial work* - [Your Organization](https://github.com/your-organization)

## 🙏 Acknowledgments

- Keycloak community for the excellent SPI documentation
- Contributors who helped improve the security and functionality
- SMS provider partners for integration support

---

**⭐ If this project helped you, please give it a star!**
