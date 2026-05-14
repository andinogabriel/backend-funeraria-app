package disenodesistemas.backendfunerariaapp.infrastructure.security.audit;

import java.util.Optional;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Sources the {@code @CreatedBy}/{@code @LastModifiedBy} value from Spring Security's thread-bound
 * {@code SecurityContext}. Spring Data JPA wires this {@link AuditorAware} bean automatically
 * thanks to the application-level {@code @EnableJpaAuditing} declaration.
 *
 * <p>The auditor is returned as the principal name reported by the active {@link Authentication}.
 * For this application that resolves to the user's email address, which matches the format the
 * audit log already uses elsewhere (e.g. {@code AuditEvent.actorEmail}).
 *
 * <p>We deliberately return {@link Optional#empty()} rather than throwing when no authenticated
 * principal is available — JPA auditing fires from a wide range of contexts (HTTP handlers, async
 * event listeners, repository unit tests, integration tests with anonymous Spring Security
 * configurations) and the call site expects a graceful "unknown auditor" rather than a runtime
 * failure that would block the persistence layer.
 */
@Component
public class SecurityContextAuditorAware implements AuditorAware<String> {

  @Override
  public Optional<String> getCurrentAuditor() {
    final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null
        || !authentication.isAuthenticated()
        || "anonymousUser".equals(authentication.getPrincipal())) {
      return Optional.empty();
    }
    final String name = authentication.getName();
    if (name == null || name.isBlank()) {
      return Optional.empty();
    }
    return Optional.of(name);
  }
}
