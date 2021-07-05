package disenodesistemas.backendfunerariaapp.security;

import disenodesistemas.backendfunerariaapp.dto.request.UserRegisterDto;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class PasswordMatchesValidator implements ConstraintValidator<PasswordMatches, Object> {

    @Override
    public void initialize(PasswordMatches constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(Object obj, ConstraintValidatorContext context) {
        UserRegisterDto user = (UserRegisterDto) obj;
        return user.getPassword().equals(user.getMatchingPassword());
    }
}
