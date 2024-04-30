package disenodesistemas.backendfunerariaapp.utils;

import disenodesistemas.backendfunerariaapp.dto.UserDto;
import disenodesistemas.backendfunerariaapp.entities.UserEntity;
import lombok.experimental.UtilityClass;

@UtilityClass
public class UserTestDataFactory {

  private static final String EMAIL = "email_test@gmail.com";
  private static final String FIRST_NAME = "Juan";
  private static final String LAST_NAME = "Perez";
  private static final String PASSWORD = "123asd312asd123";

  public static UserEntity getUserEntity() {
    final UserEntity userEntity = new UserEntity(EMAIL, FIRST_NAME, LAST_NAME, PASSWORD);
    userEntity.setId(1L);
    return userEntity;
  }

  public static UserDto getUserDto() {
    return UserDto.builder().email(EMAIL).firstName(FIRST_NAME).lastName(LAST_NAME).build();
  }
}
