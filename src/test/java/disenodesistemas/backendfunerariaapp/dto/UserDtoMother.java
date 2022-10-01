package disenodesistemas.backendfunerariaapp.dto;

import lombok.experimental.UtilityClass;

@UtilityClass
public class UserDtoMother {

    private static final String EMAIL = "email_test@gmail.com";
    private static final String FIRST_NAME = "Juan";
    private static final String LAST_NAME = "Perez";


    public static UserDto getUserDto() {
        return UserDto.builder()
                .email(EMAIL)
                .firstName(FIRST_NAME)
                .lastName(LAST_NAME)
                .build();
    }

}