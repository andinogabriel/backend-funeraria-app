package disenodesistemas.backendfunerariaapp.infrastructure.security;

import disenodesistemas.backendfunerariaapp.application.port.out.AuthenticatedUserPort;
import disenodesistemas.backendfunerariaapp.domain.entity.UserEntity;
import disenodesistemas.backendfunerariaapp.exception.AppException;
import disenodesistemas.backendfunerariaapp.exception.NotFoundException;
import disenodesistemas.backendfunerariaapp.infrastructure.persistence.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

/**
 * Bridges Spring Security's runtime authentication context with the application-facing port that
 * exposes the current principal. It resolves the authenticated email from the security context and
 * then loads the corresponding persisted user aggregate required by business use cases.
 */
@Service
@RequiredArgsConstructor
public class SpringSecurityAuthenticatedUserAdapter implements AuthenticatedUserPort {

  private final UserRepository userRepository;

  /**
   * Returns the active Spring Security authentication object for the current thread-bound request.
   * When the request is anonymous or the principal name is blank, the method fails immediately so
   * application use cases do not continue under an undefined authenticated context.
   */
  private Authentication requireAuthentication() {
    final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null || StringUtils.isBlank(authentication.getName())) {
      throw new AppException("auth.error.current.user.not.available", HttpStatus.UNAUTHORIZED);
    }
    return authentication;
  }

  /**
   * Returns the authenticated principal name exactly as exposed by Spring Security. In this
   * application the principal name is the user email, so callers can rely on this method when they
   * only need identity information without loading the full persisted aggregate.
   */
  @Override
  public String getAuthenticatedEmail() {
    return requireAuthentication().getName();
  }

  /**
   * Resolves the full authenticated {@link UserEntity} from persistence using the current
   * principal name. This gives business use cases access to the aggregate state required for
   * authorization, ownership checks and downstream domain behavior.
   */
  @Override
  public UserEntity getAuthenticatedUser() {
    return userRepository
        .findByEmail(getAuthenticatedEmail())
        .orElseThrow(() -> new NotFoundException("user.error.email.not.registered"));
  }
}
