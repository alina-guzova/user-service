package com.inno.userservice.dto.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;

public class EmailValidator implements ConstraintValidator<ValidEmail, String> {

  public static final Pattern PATTERN =
      Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

  @Override
  public boolean isValid(String email, ConstraintValidatorContext constraintValidatorContext) {
    if (StringUtils.isBlank(email)) {
      return false;
    }
    return PATTERN.matcher(email).matches();
  }
}
