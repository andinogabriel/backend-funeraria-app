package disenodesistemas.backendfunerariaapp.utils;

import lombok.Builder;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ConfirmationTokenTestData extends AppExceptionTestData {
  private final String token;

  @Builder
  public ConfirmationTokenTestData(
      final String testName,
      final HttpStatus expectedStatus,
      final String expectedMessage,
      final String token) {
    super(testName, expectedStatus, expectedMessage);
    this.token = token;
  }
}
