package disenodesistemas.backendfunerariaapp.modern.security.password;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import disenodesistemas.backendfunerariaapp.security.password.PepperedPasswordEncoder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
@DisplayName("PepperedPasswordEncoder")
class PepperedPasswordEncoderTest {

  @Mock private PasswordEncoder delegate;

  @Test
  @DisplayName(
      "Given a raw password when it is encoded then the encoder appends the configured pepper before delegating")
  void givenARawPasswordWhenItIsEncodedThenTheEncoderAppendsTheConfiguredPepperBeforeDelegating() {
    final PepperedPasswordEncoder encoder = new PepperedPasswordEncoder(delegate, "pepper-value");
    when(delegate.encode("plain-password:pepper-value")).thenReturn("encoded-password");

    final String encoded = encoder.encode("plain-password");

    assertThat(encoded).isEqualTo("encoded-password");
    verify(delegate).encode("plain-password:pepper-value");
  }

  @Test
  @DisplayName(
      "Given a raw password when it is matched then the encoder compares the peppered password through the delegate")
  void givenARawPasswordWhenItIsMatchedThenTheEncoderComparesThePepperedPasswordThroughTheDelegate() {
    final PepperedPasswordEncoder encoder = new PepperedPasswordEncoder(delegate, "pepper-value");
    when(delegate.matches("plain-password:pepper-value", "encoded-password")).thenReturn(true);

    final boolean matches = encoder.matches("plain-password", "encoded-password");

    assertThat(matches).isTrue();
    verify(delegate).matches("plain-password:pepper-value", "encoded-password");
  }

  @Test
  @DisplayName(
      "Given a stored encoded password when upgradeEncoding is requested then the encoder delegates the decision without changing the payload")
  void givenAStoredEncodedPasswordWhenUpgradeEncodingIsRequestedThenTheEncoderDelegatesTheDecisionWithoutChangingThePayload() {
    final PepperedPasswordEncoder encoder = new PepperedPasswordEncoder(delegate, "pepper-value");
    when(delegate.upgradeEncoding("encoded-password")).thenReturn(true);

    final boolean upgradeEncoding = encoder.upgradeEncoding("encoded-password");

    assertThat(upgradeEncoding).isTrue();
    verify(delegate).upgradeEncoding("encoded-password");
  }
}
