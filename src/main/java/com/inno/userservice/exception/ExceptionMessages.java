package com.inno.userservice.exception;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ExceptionMessages {

  // User exceptions
  public static final String USER_NOT_FOUND = "Failed to retrieve user with id = %d";
  public static final String USER_ALREADY_EXISTS_WITH_EMAIL = "User with email %s already exists";
  public static final String USER_ALREADY_EXISTS_WITH_ID = "User with id %s already exists";
  public static final String USER_FORBIDDEN = "You can get only your own profile";

  // PaymentCard exceptions
  public static final String PAYMENT_CARD_NOT_FOUND =
      "Failed to retrieve payment card with id = %d";
  public static final String PAYMENT_CARD_ALREADY_EXISTS_WITH_NUMBER =
      "Payment card with number %s already exists";
  public static final String PAYMENT_CARD_LIMIT_EXCEEDED =
      "User with id = %d reached maximum limit of 5 payment cards";
}
