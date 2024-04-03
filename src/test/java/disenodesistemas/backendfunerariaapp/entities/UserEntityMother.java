package disenodesistemas.backendfunerariaapp.entities;

import lombok.experimental.UtilityClass;

@UtilityClass
public class UserEntityMother {

  private static final String EMAIL = "email_test@gmail.com";
  private static final String FIRST_NAME = "Juan";
  private static final String LAST_NAME = "Perez";
  private static final String PASSWORD = "123asd312asd123";

  public static UserEntity getUser() {
    final UserEntity userEntity = new UserEntity(EMAIL, FIRST_NAME, LAST_NAME, PASSWORD);
    userEntity.setId(1L);
    return userEntity;
  }
}
