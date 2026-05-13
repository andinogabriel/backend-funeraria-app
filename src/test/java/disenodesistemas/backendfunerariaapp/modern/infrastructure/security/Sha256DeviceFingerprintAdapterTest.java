package disenodesistemas.backendfunerariaapp.modern.infrastructure.security;

import static org.assertj.core.api.Assertions.assertThat;

import disenodesistemas.backendfunerariaapp.infrastructure.security.Sha256DeviceFingerprintAdapter;
import disenodesistemas.backendfunerariaapp.infrastructure.security.config.SecurityRequestProperties;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Sha256DeviceFingerprintAdapter")
class Sha256DeviceFingerprintAdapterTest {

  private static final String SECRET = "fingerprint-secret-for-tests";

  @Test
  @DisplayName(
      "Given the strict default when two requests share device id but report different user agents then the produced fingerprints differ")
  void
      givenTheStrictDefaultWhenTwoRequestsShareDeviceIdButReportDifferentUserAgentsThenTheProducedFingerprintsDiffer() {
    final Sha256DeviceFingerprintAdapter adapter = adapter(true);

    final String desktop = adapter.fingerprint("device-1", "Mozilla/5.0 (Windows NT 10.0)");
    final String mobile = adapter.fingerprint("device-1", "Mozilla/5.0 (Linux; Android 13)");

    assertThat(desktop).isNotEqualTo(mobile);
  }

  @Test
  @DisplayName(
      "Given the user-agent contribution is disabled when two requests share device id but report different user agents then the produced fingerprints are identical")
  void
      givenTheUserAgentContributionIsDisabledWhenTwoRequestsShareDeviceIdButReportDifferentUserAgentsThenTheProducedFingerprintsAreIdentical() {
    final Sha256DeviceFingerprintAdapter adapter = adapter(false);

    final String desktop = adapter.fingerprint("device-1", "Mozilla/5.0 (Windows NT 10.0)");
    final String mobile = adapter.fingerprint("device-1", "Mozilla/5.0 (Linux; Android 13)");

    assertThat(desktop).isEqualTo(mobile);
  }

  @Test
  @DisplayName(
      "Given any toggle when the device id differs then the fingerprints still differ, because the device id is always part of the material")
  void
      givenAnyToggleWhenTheDeviceIdDiffersThenTheFingerprintsStillDifferBecauseTheDeviceIdIsAlwaysPartOfTheMaterial() {
    final Sha256DeviceFingerprintAdapter strict = adapter(true);
    final Sha256DeviceFingerprintAdapter relaxed = adapter(false);

    assertThat(strict.fingerprint("device-A", "UA"))
        .isNotEqualTo(strict.fingerprint("device-B", "UA"));
    assertThat(relaxed.fingerprint("device-A", "UA"))
        .isNotEqualTo(relaxed.fingerprint("device-B", "UA"));
  }

  private Sha256DeviceFingerprintAdapter adapter(final boolean includeUserAgent) {
    return new Sha256DeviceFingerprintAdapter(
        new SecurityRequestProperties("X-Device-Id", "Idempotency-Key", SECRET, includeUserAgent));
  }
}
