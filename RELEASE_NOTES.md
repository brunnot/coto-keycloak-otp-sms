# Release Notes - Keycloak SMS OTP Authenticator v1.0.0

## 🎉 First Production Release

This is the first stable release of the Keycloak SMS OTP Authenticator, ready for production use.

## 📦 Download

Download the JAR file from the release assets and copy it to your Keycloak `providers/` directory.

**File:** `otp-sms-1.0.0.jar`

## 🚀 Features

- **SMS OTP Authentication**: Send one-time passwords via SMS to users' mobile phones
- **Multiple SMS Providers**: Built-in support for Zenvia, with extensible architecture for other providers
- **Configurable Settings**: 
  - Code length (4-10 digits)
  - Expiration time (1 minute - 1 hour)
  - Custom phone attribute names
- **Security Features**:
  - Phone number validation
  - Input sanitization
  - Secure logging (masked phone numbers)
  - Code cleanup after authentication
- **Internationalization**: Support for English and Portuguese (Brazil)
- **Testing Mode**: Simulation mode for development without sending real SMS
- **Easy Configuration**: Admin UI configuration for all settings

## 🛠️ Quick Installation

1. Download `otp-sms-1.0.0.jar` from this release
2. Copy to your Keycloak providers directory:
   ```bash
   cp otp-sms-1.0.0.jar /opt/keycloak/providers/
   ```
3. Restart Keycloak or rebuild for production mode:
   ```bash
   /opt/keycloak/bin/kc.sh build
   ```

## ⚙️ Configuration

1. Go to **Authentication** → **Flows** in Keycloak Admin Console
2. Add **SMS Authentication** to your authentication flow
3. Configure your SMS provider settings (Zenvia credentials)
4. Ensure users have phone numbers in their profiles

## 📋 Requirements

- **Java**: 17 or higher
- **Keycloak**: 26.3.4 (compatible with other recent versions)
- **SMS Provider**: Account with Zenvia or custom implementation

## 🔐 Security Improvements

This release includes several security enhancements:
- Phone numbers are masked in logs to protect user privacy
- Input validation prevents malicious code injection
- OTP codes are automatically cleaned from session after use
- Phone number format validation ensures data integrity

## 📚 Documentation

Complete documentation is available in the [README.md](README.md) file, including:
- Detailed installation instructions
- Configuration examples
- Security considerations
- Troubleshooting guide

## 🐛 Bug Reports

If you encounter any issues, please report them on the [Issues](../../issues) page.

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

**Ready for production use! 🎯**
