package disenodesistemas.backendfunerariaapp.utils;

import org.apache.commons.lang3.StringUtils;

/**
 * Masks personal data before it reaches a log line. Logs are shipped to aggregation, kept for
 * weeks and read by people who do not need the raw values, so identifiers such as email
 * addresses are reduced to a form that still lets an operator correlate events
 * ({@code j***@example.com}) without exposing the full address.
 */
public final class LogMasking {

  private static final String MASK = "***";

  private LogMasking() {}

  /**
   * Keeps the first character of the local part and the full domain. Blank input returns an
   * empty string; a value without {@code @} is masked entirely.
   */
  public static String maskEmail(final String email) {
    if (StringUtils.isBlank(email)) {
      return "";
    }
    final int at = email.indexOf('@');
    if (at <= 0) {
      return MASK;
    }
    return email.charAt(0) + MASK + email.substring(at);
  }
}
