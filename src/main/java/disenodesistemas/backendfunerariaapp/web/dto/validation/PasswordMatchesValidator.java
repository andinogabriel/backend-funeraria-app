package disenodesistemas.backendfunerariaapp.web.dto.validation;

import disenodesistemas.backendfunerariaapp.web.dto.request.UserRegisterDto;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PasswordMatchesValidator implements ConstraintValidator<PasswordMatches, Object> {

  @Override
  public void initialize(final PasswordMatches constraintAnnotation) {
    ConstraintValidator.super.initialize(constraintAnnotation);
  }

  @Override
  public boolean isValid(final Object obj, final ConstraintValidatorContext context) {
    final UserRegisterDto user = (UserRegisterDto) obj;
    return user.password().equals(user.matchingPassword());
  }
}
