package disenodesistemas.backendfunerariaapp.modern.utils;

import static org.assertj.core.api.Assertions.assertThat;

import disenodesistemas.backendfunerariaapp.utils.LogMasking;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class LogMaskingTest {

  @Test
  @DisplayName("Given an email when masking then only the first local char and the domain remain")
  void masksEmail() {
    assertThat(LogMasking.maskEmail("gabriel@funeraria.com")).isEqualTo("g***@funeraria.com");
  }

  @Test
  @DisplayName("Given a blank value when masking then an empty string is returned")
  void blankReturnsEmpty() {
    assertThat(LogMasking.maskEmail(" ")).isEmpty();
    assertThat(LogMasking.maskEmail(null)).isEmpty();
  }

  @Test
  @DisplayName("Given a value without @ when masking then it is fully masked")
  void noAtIsFullyMasked() {
    assertThat(LogMasking.maskEmail("not-an-email")).isEqualTo("***");
    assertThat(LogMasking.maskEmail("@domain.com")).isEqualTo("***");
  }
}
