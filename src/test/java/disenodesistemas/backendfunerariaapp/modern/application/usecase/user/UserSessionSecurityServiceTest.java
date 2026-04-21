package disenodesistemas.backendfunerariaapp.modern.application.usecase.user;

import static org.mockito.Mockito.verify;

import disenodesistemas.backendfunerariaapp.application.model.RequestMetadata;
import disenodesistemas.backendfunerariaapp.application.port.out.SecurityThreatProtectionPort;
import disenodesistemas.backendfunerariaapp.application.usecase.user.UserSessionSecurityService;
import disenodesistemas.backendfunerariaapp.modern.support.SecurityTestDataFactory;
import disenodesistemas.backendfunerariaapp.security.ratelimit.LoginRateLimiter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserSessionSecurityService")
class UserSessionSecurityServiceTest {

  @Mock private SecurityThreatProtectionPort securityThreatProtectionPort;
  @Mock private LoginRateLimiter loginRateLimiter;

  @InjectMocks private UserSessionSecurityService userSessionSecurityService;

  @Test
  @DisplayName(
      "Given a login request when the security guard validates the request then it checks both adaptive protection and rate limiting")
  void givenALoginRequestWhenTheSecurityGuardValidatesTheRequestThenItChecksBothAdaptiveProtectionAndRateLimiting() {
    final RequestMetadata requestMetadata = SecurityTestDataFactory.requestMetadata();

    userSessionSecurityService.assertLoginAllowed("john.doe@example.com", "device-123", requestMetadata);

    verify(securityThreatProtectionPort)
        .assertLoginAllowed("john.doe@example.com", "device-123", requestMetadata);
    verify(loginRateLimiter).assertAllowed("john.doe@example.com", requestMetadata.ipAddress());
  }

  @Test
  @DisplayName(
      "Given a failed login attempt when the security guard records it then it increments rate limits and adaptive threat counters")
  void givenAFailedLoginAttemptWhenTheSecurityGuardRecordsItThenItIncrementsRateLimitsAndAdaptiveThreatCounters() {
    final RequestMetadata requestMetadata = SecurityTestDataFactory.requestMetadata();

    userSessionSecurityService.recordLoginFailure(
        "john.doe@example.com", "device-123", requestMetadata);

    verify(loginRateLimiter).onFailure("john.doe@example.com", requestMetadata.ipAddress());
    verify(securityThreatProtectionPort)
        .recordLoginFailure("john.doe@example.com", "device-123", requestMetadata);
  }
}
