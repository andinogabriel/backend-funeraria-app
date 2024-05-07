package disenodesistemas.backendfunerariaapp.utils;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public abstract class AppExceptionTestData {
  public String testName;
  public HttpStatus expectedStatus;
  public String expectedMessage;

  public AppExceptionTestData(
      final String testName, final HttpStatus expectedStatus, final String expectedMessage) {
    this.testName = testName;
    this.expectedStatus = expectedStatus;
    this.expectedMessage = expectedMessage;
  }

  @Override
  public String toString() {
    return testName;
  }
}
