package disenodesistemas.backendfunerariaapp.security.password;

import java.util.Objects;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Decorates another {@link PasswordEncoder} by appending an application-level pepper before any
 * hash or verification is performed. This adds a server-side secret to password processing so the
 * persisted hash remains harder to reuse if the database contents are exposed.
 */
public final class PepperedPasswordEncoder implements PasswordEncoder {

  private static final String DELIMITER = ":";
  private final PasswordEncoder delegate;
  private final String pepper;

  /**
   * Creates the password encoder decorator that injects the configured pepper into every password
   * operation. The wrapped encoder remains responsible for the actual hash algorithm while this
   * class ensures the server-side secret is consistently appended first.
   */
  public PepperedPasswordEncoder(final PasswordEncoder delegate, final String pepper) {
    this.delegate = Objects.requireNonNull(delegate, "delegate");
    this.pepper = Objects.requireNonNull(pepper, "pepper");
  }

  /**
   * Hashes the raw password after applying the configured pepper strategy. The method keeps the
   * public contract identical to a normal {@link PasswordEncoder} while ensuring the delegate never
   * receives the unpeppered credential value.
   */
  @Override
  public String encode(final CharSequence rawPassword) {
    return delegate.encode(applyPepper(rawPassword));
  }

  /**
   * Compares the raw password against the stored hash using the same pepper transformation applied
   * during encoding. This guarantees verification stays symmetric with hashing even when the
   * underlying encoder changes implementation details.
   */
  @Override
  public boolean matches(final CharSequence rawPassword, final String encodedPassword) {
    return delegate.matches(applyPepper(rawPassword), encodedPassword);
  }

  /**
   * Delegates the encoding-upgrade decision to the wrapped encoder implementation. The pepper does
   * not influence upgrade semantics directly, so this method simply preserves the delegate's
   * behavior while keeping the type compatible with Spring Security contracts.
   */
  @Override
  public boolean upgradeEncoding(final String encodedPassword) {
    return delegate.upgradeEncoding(encodedPassword);
  }

  /**
   * Applies the configured pepper to the raw password while preserving null-input behavior. This
   * helper centralizes how the secret is concatenated so encode and match remain perfectly
   * consistent and easy to audit.
   */
  private CharSequence applyPepper(final CharSequence rawPassword) {
    if (rawPassword == null) return null;
    return rawPassword + DELIMITER + pepper;
  }
}
