package disenodesistemas.backendfunerariaapp.utils;

import disenodesistemas.backendfunerariaapp.dto.request.UserLoginDto;
import lombok.Builder;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class UserTestDataLogin extends AppExceptionTestData {
  private final UserLoginDto userLoginDto;

  @Builder
  public UserTestDataLogin(
      final String testName,
      final HttpStatus expectedStatus,
      final String expectedMessage,
      final UserLoginDto userLoginDto) {
    super(testName, expectedStatus, expectedMessage);
    this.userLoginDto = userLoginDto;
  }
}
