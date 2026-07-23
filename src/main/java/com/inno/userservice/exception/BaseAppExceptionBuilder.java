package com.inno.userservice.exception;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class BaseAppExceptionBuilder {

  public static BaseAppException buildNotFoundException(String messagePattern, Object... args) {
    return new BaseAppException(messagePattern.formatted(args), HttpStatus.NOT_FOUND);
  }

  public static BaseAppException buildConflictException(String messagePattern, Object... args) {
    return new BaseAppException(messagePattern.formatted(args), HttpStatus.CONFLICT);
  }
}
