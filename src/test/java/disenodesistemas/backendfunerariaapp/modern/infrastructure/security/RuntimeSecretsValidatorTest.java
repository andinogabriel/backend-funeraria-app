package disenodesistemas.backendfunerariaapp.modern.infrastructure.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import disenodesistemas.backendfunerariaapp.infrastructure.security.RuntimeSecretsValidator;
import disenodesistemas.backendfunerariaapp.infrastructure.security.config.JwtProperties;
import disenodesistemas.backendfunerariaapp.infrastructure.security.config.PasswordSecurityProperties;
import disenodesistemas.backendfunerariaapp.infrastructure.security.config.SecurityRequestProperties;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

@DisplayName("RuntimeSecretsValidator")
class RuntimeSecretsValidatorTest {

  // 64 ASCII chars / 512 bits — meets the HS512 minimum enforced by JJWT and reflected in
  // RuntimeSecretsValidator#MIN_JWT_SECRET_LENGTH. Shorter values are tested explicitly in
  // their own scenarios.
  private static final String VALID_JWT_SECRET =
      "production-grade-jwt-secret-with-at-least-five-hundred-twelve-bits";
  private static final String VALID_PEPPER = "production-grade-pepper-secret";
  private static final String VALID_FINGERPRINT_SECRET =
      "production-grade-fingerprint-secret";

  @Test
  @DisplayName(
      "Given properly configured production secrets when the validator runs then it accepts the boot")
  void givenProperlyConfiguredProductionSecretsWhenTheValidatorRunsThenItAcceptsTheBoot() {
    final RuntimeSecretsValidator validator =
        validator(
            jwtProperties(VALID_JWT_SECRET),
            passwordProperties(VALID_PEPPER),
            requestProperties(VALID_FINGERPRINT_SECRET),
            "prod");

    assertThatCode(validator::validateOnStartup).doesNotThrowAnyException();
  }

  @Test
  @DisplayName(
      "Given the JWT placeholder is still set when the prod profile is active then the validator aborts the boot")
  void givenTheJwtPlaceholderIsStillSetWhenTheProdProfileIsActiveThenTheValidatorAbortsTheBoot() {
    final RuntimeSecretsValidator validator =
        validator(
            jwtProperties(RuntimeSecretsValidator.JWT_PLACEHOLDER),
            passwordProperties(VALID_PEPPER),
            requestProperties(VALID_FINGERPRINT_SECRET),
            "prod");

    assertThatThrownBy(validator::validateOnStartup)
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("jwt-token.secret")
        .hasMessageContaining("development placeholder");
  }

  @Test
  @DisplayName(
      "Given the password pepper placeholder is still set when the prod profile is active then the validator aborts the boot")
  void givenThePasswordPepperPlaceholderIsStillSetWhenTheProdProfileIsActiveThenTheValidatorAbortsTheBoot() {
    final RuntimeSecretsValidator validator =
        validator(
            jwtProperties(VALID_JWT_SECRET),
            passwordProperties(RuntimeSecretsValidator.PEPPER_PLACEHOLDER),
            requestProperties(VALID_FINGERPRINT_SECRET),
            "prod");

    assertThatThrownBy(validator::validateOnStartup)
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("security.password.pepper");
  }

  @Test
  @DisplayName(
      "Given the fingerprint secret placeholder is still set when the prod profile is active then the validator aborts the boot")
  void givenTheFingerprintSecretPlaceholderIsStillSetWhenTheProdProfileIsActiveThenTheValidatorAbortsTheBoot() {
    final RuntimeSecretsValidator validator =
        validator(
            jwtProperties(VALID_JWT_SECRET),
            passwordProperties(VALID_PEPPER),
            requestProperties(RuntimeSecretsValidator.FINGERPRINT_PLACEHOLDER),
            "prod");

    assertThatThrownBy(validator::validateOnStartup)
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("security.request.fingerprint-secret");
  }

  @Test
  @DisplayName(
      "Given multiple placeholders are still set when the prod profile is active then a single exception lists every finding")
  void givenMultiplePlaceholdersAreStillSetWhenTheProdProfileIsActiveThenASingleExceptionListsEveryFinding() {
    final RuntimeSecretsValidator validator =
        validator(
            jwtProperties(RuntimeSecretsValidator.JWT_PLACEHOLDER),
            passwordProperties(RuntimeSecretsValidator.PEPPER_PLACEHOLDER),
            requestProperties(RuntimeSecretsValidator.FINGERPRINT_PLACEHOLDER),
            "prod");

    assertThatThrownBy(validator::validateOnStartup)
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("jwt-token.secret")
        .hasMessageContaining("security.password.pepper")
        .hasMessageContaining("security.request.fingerprint-secret");
  }

  @Test
  @DisplayName(
      "Given a secret shorter than the minimum length when the prod profile is active then the validator aborts the boot")
  void givenASecretShorterThanTheMinimumLengthWhenTheProdProfileIsActiveThenTheValidatorAbortsTheBoot() {
    final RuntimeSecretsValidator validator =
        validator(
            jwtProperties("short"),
            passwordProperties(VALID_PEPPER),
            requestProperties(VALID_FINGERPRINT_SECRET),
            "prod");

    assertThatThrownBy(validator::validateOnStartup)
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("jwt-token.secret")
        .hasMessageContaining("minimum required length");
  }

  @Test
  @DisplayName(
      "Given placeholders are still set but the active profile is not strict when the validator runs then it does not abort the boot")
  void givenPlaceholdersAreStillSetButTheActiveProfileIsNotStrictWhenTheValidatorRunsThenItDoesNotAbortTheBoot() {
    final RuntimeSecretsValidator validator =
        validator(
            jwtProperties(RuntimeSecretsValidator.JWT_PLACEHOLDER),
            passwordProperties(RuntimeSecretsValidator.PEPPER_PLACEHOLDER),
            requestProperties(RuntimeSecretsValidator.FINGERPRINT_PLACEHOLDER),
            "dev");

    assertThatCode(validator::validateOnStartup).doesNotThrowAnyException();
  }

  @Test
  @DisplayName(
      "Given a blank secret when the prod profile is active then the validator aborts the boot")
  void givenABlankSecretWhenTheProdProfileIsActiveThenTheValidatorAbortsTheBoot() {
    final RuntimeSecretsValidator validator =
        validator(
            jwtProperties(VALID_JWT_SECRET),
            passwordProperties("   "),
            requestProperties(VALID_FINGERPRINT_SECRET),
            "prod");

    assertThatThrownBy(validator::validateOnStartup)
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("security.password.pepper")
        .hasMessageContaining("blank");
  }

  @Test
  @DisplayName(
      "Given the production profile is active with valid secrets when the active profiles are inspected then no findings are emitted")
  void givenTheProductionProfileIsActiveWithValidSecretsWhenTheActiveProfilesAreInspectedThenNoFindingsAreEmitted() {
    final RuntimeSecretsValidator validator =
        validator(
            jwtProperties(VALID_JWT_SECRET),
            passwordProperties(VALID_PEPPER),
            requestProperties(VALID_FINGERPRINT_SECRET),
            "production");

    assertThatCode(validator::validateOnStartup).doesNotThrowAnyException();
    assertThat(VALID_JWT_SECRET.length()).isGreaterThanOrEqualTo(64);
  }

  private static RuntimeSecretsValidator validator(
      final JwtProperties jwt,
      final PasswordSecurityProperties password,
      final SecurityRequestProperties request,
      final String... activeProfiles) {
    final MockEnvironment environment = new MockEnvironment();
    environment.setActiveProfiles(activeProfiles);
    return new RuntimeSecretsValidator(jwt, password, request, environment);
  }

  private static JwtProperties jwtProperties(final String secret) {
    return new JwtProperties(
        secret,
        "authorities",
        900L,
        "Bearer",
        "Authorization",
        "device_id",
        "device_fingerprint",
        "device_version");
  }

  private static PasswordSecurityProperties passwordProperties(final String pepper) {
    return new PasswordSecurityProperties(pepper, 16, 32, 1, 65536, 3);
  }

  private static SecurityRequestProperties requestProperties(final String fingerprintSecret) {
    return new SecurityRequestProperties(
        "X-Device-Id", "Idempotency-Key", fingerprintSecret);
  }
}
