package com.inno.userservice.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class BaseAppException extends RuntimeException {

  private final HttpStatus status;

  public BaseAppException(String message, HttpStatus status) {
    super(message);
    this.status = status;
  }
}
