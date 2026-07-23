package com.inno.userservice.util;

import com.inno.userservice.dto.request.PaymentCardCreateRequestDTO;
import com.inno.userservice.dto.request.PaymentCardUpdateRequestDTO;
import com.inno.userservice.dto.response.PaymentCardResponseDTO;
import com.inno.userservice.entity.PaymentCard;
import com.inno.userservice.entity.User;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

public final class PaymentCardTestData {

  // --- API Paths ---
  public static final String PAYMENT_CARDS_API_PATH = "/api/v1/payment-cards";
  public static final String CARD_BY_ID_API_PATH = "/api/v1/payment-cards/{cardId}";
  public static final String CARDS_BY_USER_API_PATH = "/api/v1/payment-cards/user/{userId}";
  public static final String ACTIVATE_CARD_API_PATH = "/api/v1/payment-cards/{cardId}/activate";
  public static final String DEACTIVATE_CARD_API_PATH = "/api/v1/payment-cards/{cardId}/deactivate";

  // --- Test Identifiers & Numbers ---
  public static final Long DEFAULT_CARD_ID = 1L;
  public static final Long NON_EXISTENT_CARD_ID = 99999L;
  public static final String DEFAULT_NUMBER = "1234567890123456";
  public static final String EXISTING_CARD_NUMBER = "1234567890123456";
  public static final String MASKED_CARD_NUMBER = "**** **** **** 3456";
  public static final String SECOND_CARD_NUMBER = "9876543210987654";
  public static final String SECOND_MASKED_CARD_NUMBER = "**** **** **** 7654";
  public static final String NEW_CARD_NUMBER_PREFIX = "100000000000000";

  // --- Card Attributes ---
  public static final String DEFAULT_HOLDER = "JOHN DOE";
  public static final String DEFAULT_EXPIRATION_DATE_STRING = "12/28";
  public static final LocalDate DEFAULT_EXPIRATION_DATE = LocalDate.of(2028, 12, 31);
  public static final Boolean DEFAULT_ACTIVE = true;
  public static final OffsetDateTime DEFAULT_CREATED_AT =
      OffsetDateTime.parse("2026-01-01T10:00:00Z");
  public static final OffsetDateTime DEFAULT_UPDATED_AT =
      OffsetDateTime.parse("2026-01-01T10:00:00Z");
  public static final String UPDATED_HOLDER = "JANE DOE";
  public static final String UPDATED_EXPIRATION_DATE_STRING = "05/30";

  private PaymentCardTestData() {}

  public static PaymentCardCreateRequestDTO buildCreateRequestDto() {
    return buildCreateRequestDto(DEFAULT_NUMBER);
  }

  public static PaymentCardCreateRequestDTO buildCreateRequestDto(String number) {
    return PaymentCardCreateRequestDTO.builder()
        .userId(UserTestData.DEFAULT_ID)
        .number(number)
        .holder(DEFAULT_HOLDER)
        .expirationDate(DEFAULT_EXPIRATION_DATE_STRING)
        .build();
  }

  public static PaymentCard buildPaymentCardEntity(User user) {
    return buildPaymentCardEntity(DEFAULT_CARD_ID, DEFAULT_NUMBER, user);
  }

  public static PaymentCard buildPaymentCardEntity(Long id, String number, User user) {
    return PaymentCard.builder()
        .id(id)
        .number(number)
        .holder(DEFAULT_HOLDER)
        .expirationDate(DEFAULT_EXPIRATION_DATE)
        .active(DEFAULT_ACTIVE)
        .user(user)
        .build();
  }

  public static PaymentCardResponseDTO buildResponseDto() {
    return buildResponseDto(DEFAULT_CARD_ID, DEFAULT_NUMBER);
  }

  public static PaymentCardResponseDTO buildResponseDto(Long id, String number) {
    return PaymentCardResponseDTO.builder()
        .id(id)
        .number(number)
        .holder(DEFAULT_HOLDER)
        .expirationDate(DEFAULT_EXPIRATION_DATE_STRING)
        .active(DEFAULT_ACTIVE)
        .createdAt(DEFAULT_CREATED_AT)
        .updatedAt(DEFAULT_UPDATED_AT)
        .build();
  }

  public static PaymentCardUpdateRequestDTO buildUpdateRequestDto() {
    return buildUpdateRequestDto(UPDATED_HOLDER, UPDATED_EXPIRATION_DATE_STRING);
  }

  public static PaymentCardUpdateRequestDTO buildUpdateRequestDto(
      String holder, String expirationDate) {
    return PaymentCardUpdateRequestDTO.builder()
        .holder(holder)
        .expirationDate(expirationDate)
        .build();
  }

  public static Page<PaymentCard> buildPage(User user) {
    return new PageImpl<>(List.of(buildPaymentCardEntity(user)));
  }

  public static List<PaymentCard> buildPaymentCardList(User user) {
    return List.of(buildPaymentCardEntity(user));
  }
}
