package disenodesistemas.backendfunerariaapp.infrastructure.security;

import disenodesistemas.backendfunerariaapp.infrastructure.security.config.JwtProperties;
import disenodesistemas.backendfunerariaapp.infrastructure.security.config.PasswordSecurityProperties;
import disenodesistemas.backendfunerariaapp.infrastructure.security.config.SecurityRequestProperties;
import jakarta.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * Validates that runtime security secrets ({@code JWT_TOKEN_SECRET},
 * {@code SECURITY_PASSWORD_PEPPER} and {@code SECURITY_REQUEST_FINGERPRINT_SECRET}) are not
 * left at their development placeholder values when the application starts in a production
 * profile. The validator runs as part of the Spring lifecycle so a misconfigured deployment
 * fails fast at boot time with a localized error message instead of silently serving traffic
 * with predictable secrets.
 *
 * <p>Behaviour summary:
 *
 * <ul>
 *   <li>Profile {@code prod}: any placeholder or blank secret aborts the boot with an
 *       {@link IllegalStateException}.
 *   <li>Any other profile (including {@code default}, {@code dev}, {@code test} or
 *       {@code docker}): the same findings are logged as a structured warning so contributors
 *       still see the signal locally, but the application keeps running.
 * </ul>
 *
 * <p>The placeholder catalogue mirrors the defaults declared in {@code application.yaml} and
 * {@code application.properties}; whenever those defaults change, the corresponding entry here
 * must change too.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RuntimeSecretsValidator {

  /** Profiles where boot must abort on any placeholder secret. */
  private static final Set<String> STRICT_PROFILES = Set.of("prod", "production");

  /** Minimum acceptable secret length for the pepper and fingerprint secrets. */
  private static final int MIN_SECRET_LENGTH = 16;

  /**
   * Minimum acceptable length for the JWT signing secret. JJWT 0.13+ enforces RFC 7518 §3.2
   * strictly: the HMAC-SHA key MUST be at least the size of the hash output. JwtProvider signs
   * with {@code Jwts.SIG.HS512}, so the minimum is 512 bits / 64 ASCII chars. Anything shorter
   * makes {@code DefaultMacAlgorithm.validateKey(...)} throw a {@code WeakKeyException} at the
   * first sign call — i.e. on the very first login request, after a clean boot. The validator
   * surfaces the same constraint at startup time so the failure happens as part of the boot
   * sequence rather than as a confusing 500 on the first inbound request.
   *
   * <p>If the signing algorithm is ever downgraded to HS256, lower this to 32; for HS384 use
   * 48. The constant lives next to its usage on purpose so the link to the algorithm choice
   * is visible from one place.
   */
  private static final int MIN_JWT_SECRET_LENGTH = 64;

  /**
   * Known development placeholders for the secrets the validator inspects. These constants are
   * exposed publicly on purpose so tests and developer tooling can reference the exact values
   * declared in {@code application.yaml} without duplicating string literals.
   */
  public static final String JWT_PLACEHOLDER = "change-this-jwt-secret-in-production";

  public static final String PEPPER_PLACEHOLDER = "change-me";

  public static final String FINGERPRINT_PLACEHOLDER = "change-me-too";

  private final JwtProperties jwtProperties;
  private final PasswordSecurityProperties passwordSecurityProperties;
  private final SecurityRequestProperties securityRequestProperties;
  private final Environment environment;

  /**
   * Runs the validation pipeline once the bean is fully wired. Intentional reliance on Spring's
   * lifecycle keeps the check as part of the standard boot sequence so failures surface together
   * with other configuration errors instead of in an isolated runner phase. Public visibility lets
   * tests outside the package call the same lifecycle hook directly without reflection.
   */
  @PostConstruct
  public void validateOnStartup() {
    final List<String> findings = collectFindings();
    if (findings.isEmpty()) {
      return;
    }

    if (isStrictProfileActive()) {
      findings.forEach(
          finding ->
              log.atError()
                  .addKeyValue("event", "security.secrets.invalid")
                  .addKeyValue("finding", finding)
                  .log("security.secrets.invalid"));
      throw new IllegalStateException(
          "Refusing to start: insecure runtime secrets detected -> "
              + String.join("; ", findings));
    }

    findings.forEach(
        finding ->
            log.atWarn()
                .addKeyValue("event", "security.secrets.weak")
                .addKeyValue("finding", finding)
                .addKeyValue("activeProfiles", String.join(",", environment.getActiveProfiles()))
                .log("security.secrets.weak"));
  }

  /**
   * Collects every secret-related issue found in the current environment. Returning a list lets
   * the caller decide how to react (fail vs. warn) and keeps the validator focused on detection
   * rather than on policy enforcement.
   */
  private List<String> collectFindings() {
    final List<String> findings = new ArrayList<>();
    appendIfPlaceholder(
        findings,
        "jwt-token.secret",
        jwtProperties.secret(),
        JWT_PLACEHOLDER,
        MIN_JWT_SECRET_LENGTH);
    appendIfPlaceholder(
        findings,
        "security.password.pepper",
        passwordSecurityProperties.pepper(),
        PEPPER_PLACEHOLDER,
        MIN_SECRET_LENGTH);
    appendIfPlaceholder(
        findings,
        "security.request.fingerprint-secret",
        securityRequestProperties.fingerprintSecret(),
        FINGERPRINT_PLACEHOLDER,
        MIN_SECRET_LENGTH);
    return findings;
  }

  /**
   * Records a finding whenever the supplied secret is blank, equal to its known placeholder or
   * shorter than the minimum acceptable length. The message intentionally never echoes the secret
   * itself so logs and exceptions stay safe to ship to operators.
   */
  private void appendIfPlaceholder(
      final List<String> findings,
      final String propertyName,
      final String actualValue,
      final String placeholder,
      final int minLength) {
    if (StringUtils.isBlank(actualValue)) {
      findings.add(propertyName + " is blank");
      return;
    }
    if (StringUtils.equals(actualValue, placeholder)) {
      findings.add(propertyName + " is still set to the development placeholder");
      return;
    }
    if (actualValue.length() < minLength) {
      findings.add(
          propertyName
              + " is shorter than the minimum required length of "
              + minLength
              + " characters");
    }
  }

  /**
   * Returns {@code true} when at least one Spring profile that the project considers
   * production-grade is active. Keeping the predicate centralized makes it trivial to extend the
   * strict policy to additional environment names without touching the main validation method.
   */
  private boolean isStrictProfileActive() {
    for (final String profile : environment.getActiveProfiles()) {
      if (STRICT_PROFILES.contains(profile)) {
        return true;
      }
    }
    return false;
  }
}
