package disenodesistemas.backendfunerariaapp.modern.infrastructure.security;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import disenodesistemas.backendfunerariaapp.exception.AppException;
import disenodesistemas.backendfunerariaapp.infrastructure.security.AdaptiveThreatProtectionAdapter;
import disenodesistemas.backendfunerariaapp.modern.support.SecurityTestDataFactory;
import disenodesistemas.backendfunerariaapp.modern.support.TestValues;
import disenodesistemas.backendfunerariaapp.security.threat.ThreatProtectionProperties;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("AdaptiveThreatProtectionAdapter")
class AdaptiveThreatProtectionAdapterTest {

  private final AdaptiveThreatProtectionAdapter adapter =
      new AdaptiveThreatProtectionAdapter(new ThreatProtectionProperties(3, 2, 3600, true));
  private final AdaptiveThreatProtectionAdapter thresholdBasedAdapter =
      new AdaptiveThreatProtectionAdapter(new ThreatProtectionProperties(3, 2, 3600, false));

  @Test
  @DisplayName(
      "Given repeated failed login attempts when the threshold is reached then the principal is temporarily blacklisted")
  void givenRepeatedFailedLoginAttemptsWhenTheThresholdIsReachedThenThePrincipalIsTemporarilyBlacklisted() {
    final var requestMetadata = SecurityTestDataFactory.requestMetadata();

    adapter.recordLoginFailure(TestValues.USER_EMAIL, TestValues.DEVICE_ID, requestMetadata);
    adapter.recordLoginFailure(TestValues.USER_EMAIL, TestValues.DEVICE_ID, requestMetadata);
    adapter.recordLoginFailure(TestValues.USER_EMAIL, TestValues.DEVICE_ID, requestMetadata);

    assertThatThrownBy(
            () ->
                adapter.assertLoginAllowed(
                    TestValues.USER_EMAIL, TestValues.DEVICE_ID, requestMetadata))
        .isInstanceOf(AppException.class)
        .extracting("message")
        .isEqualTo("security.blacklist.error.blocked");
  }

  @Test
  @DisplayName(
      "Given a principal with a recorded failure when the login succeeds then the adapter clears the accumulated failures and allows the next request")
  void givenAPrincipalWithARecordedFailureWhenTheLoginSucceedsThenTheAdapterClearsTheAccumulatedFailuresAndAllowsTheNextRequest() {
    final var requestMetadata = SecurityTestDataFactory.requestMetadata();

    adapter.recordLoginFailure(TestValues.USER_EMAIL, TestValues.DEVICE_ID, requestMetadata);
    adapter.recordLoginSuccess(TestValues.USER_EMAIL, TestValues.DEVICE_ID, requestMetadata);

    assertThatNoException()
        .isThrownBy(
            () ->
                adapter.assertLoginAllowed(
                    TestValues.USER_EMAIL, TestValues.DEVICE_ID, requestMetadata));
  }

  @Test
  @DisplayName(
      "Given a suspicious device mismatch when the request is recorded then the request is immediately blocked by the blacklist")
  void givenASuspiciousDeviceMismatchWhenTheRequestIsRecordedThenTheRequestIsImmediatelyBlockedByTheBlacklist() {
    final var requestMetadata = SecurityTestDataFactory.requestMetadata();

    adapter.recordSuspiciousRequest(
        TestValues.USER_EMAIL, TestValues.ALTERNATE_DEVICE_ID, requestMetadata, "device_id_mismatch");

    assertThatThrownBy(
            () ->
                adapter.assertRequestAllowed(
                    TestValues.USER_EMAIL, TestValues.ALTERNATE_DEVICE_ID, requestMetadata))
        .isInstanceOf(AppException.class)
        .extracting("message")
        .isEqualTo("security.blacklist.error.blocked");
  }

  @Test
  @DisplayName(
      "Given repeated suspicious requests when immediate blacklisting is disabled and the threshold is reached then the adapter blocks the principal on the next request")
  void givenRepeatedSuspiciousRequestsWhenImmediateBlacklistingIsDisabledAndTheThresholdIsReachedThenTheAdapterBlocksThePrincipalOnTheNextRequest() {
    final var requestMetadata = SecurityTestDataFactory.requestMetadata();

    thresholdBasedAdapter.recordSuspiciousRequest(
        TestValues.USER_EMAIL, TestValues.ALTERNATE_DEVICE_ID, requestMetadata, "ip_rotation");
    assertThatNoException()
        .isThrownBy(
            () ->
                thresholdBasedAdapter.assertRequestAllowed(
                    TestValues.USER_EMAIL, TestValues.ALTERNATE_DEVICE_ID, requestMetadata));

    thresholdBasedAdapter.recordSuspiciousRequest(
        TestValues.USER_EMAIL, TestValues.ALTERNATE_DEVICE_ID, requestMetadata, "ip_rotation");

    assertThatThrownBy(
            () ->
                thresholdBasedAdapter.assertRequestAllowed(
                    TestValues.USER_EMAIL, TestValues.ALTERNATE_DEVICE_ID, requestMetadata))
        .isInstanceOf(AppException.class)
        .extracting("message")
        .isEqualTo("security.blacklist.error.blocked");
  }
}
