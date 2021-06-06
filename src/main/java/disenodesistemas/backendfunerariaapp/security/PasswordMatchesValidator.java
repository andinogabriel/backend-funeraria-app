package disenodesistemas.backendfunerariaapp.security;

import disenodesistemas.backendfunerariaapp.dto.UserDto;
import disenodesistemas.backendfunerariaapp.models.requests.UserDetailsRequestModel;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class PasswordMatchesValidator implements ConstraintValidator<PasswordMatches, Object> {

    @Override
    public void initialize(PasswordMatches constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(Object obj, ConstraintValidatorContext context) {
        UserDetailsRequestModel user = (UserDetailsRequestModel) obj;
        return user.getPassword().equals(user.getMatchingPassword());
    }
}
