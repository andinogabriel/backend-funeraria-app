package disenodesistemas.backendfunerariaapp.security;

import disenodesistemas.backendfunerariaapp.dto.request.UserRegisterDto;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class PasswordMatchesValidator implements ConstraintValidator<PasswordMatches, Object> {

  @Override
  public void initialize(final PasswordMatches constraintAnnotation) {
    ConstraintValidator.super.initialize(constraintAnnotation);
  }

  @Override
  public boolean isValid(final Object obj, final ConstraintValidatorContext context) {
    final UserRegisterDto user = (UserRegisterDto) obj;
    return user.getPassword().equals(user.getMatchingPassword());
  }
}
