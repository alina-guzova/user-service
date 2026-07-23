package com.inno.userservice.util;

import com.inno.userservice.dto.request.UserCreateRequestDTO;
import com.inno.userservice.dto.request.UserFilterDTO;
import com.inno.userservice.dto.request.UserUpdateRequestDTO;
import com.inno.userservice.dto.response.UserResponseDTO;
import com.inno.userservice.entity.User;
import java.time.LocalDate;

public final class UserTestData {

  // --- API Paths ---
  public static final String USERS_API_PATH = "/api/v1/users";
  public static final String USER_BY_ID_API_PATH = "/api/v1/users/{userId}";
  public static final String ACTIVATE_USER_API_PATH = "/api/v1/users/{userId}/activate";
  public static final String DEACTIVATE_USER_API_PATH = "/api/v1/users/{userId}/deactivate";

  // --- Cache Keys ---
  public static final String REDIS_CACHE_KEY_PREFIX = "users::";

  // --- Test Identifiers & Emails ---
  public static final Long DEFAULT_ID = 1L;
  public static final Long NON_EXISTENT_USER_ID = 99999L;
  public static final String DEFAULT_EMAIL = "alice@gmail.com";
  public static final String EXISTING_EMAIL = "existing.alice@gmail.com";
  public static final String ANOTHER_EMAIL = "bob@gmail.com";

  // --- User Attributes ---
  public static final String DEFAULT_NAME = "Alice";
  public static final String DEFAULT_SURNAME = "Britt";
  public static final LocalDate DEFAULT_BIRTH_DATE = LocalDate.of(2000, 1, 23);
  public static final String UPDATED_NAME = "Viktoria";
  public static final String UPDATED_SURNAME = "Sviridova";

  // --- Search Parameters ---
  public static final String SEARCH_NAME = "Alice";
  public static final String SEARCH_SURNAME = "Britt";

  private UserTestData() {}

  public static UserCreateRequestDTO buildCreateRequestDto() {
    return buildCreateRequestDto(DEFAULT_EMAIL);
  }

  public static UserCreateRequestDTO buildCreateRequestDto(String email) {
    return UserCreateRequestDTO.builder()
        .name(DEFAULT_NAME)
        .surname(DEFAULT_SURNAME)
        .birthDate(DEFAULT_BIRTH_DATE)
        .email(email)
        .build();
  }

  public static User buildUserEntity() {
    return buildUserEntity(DEFAULT_ID, DEFAULT_EMAIL);
  }

  public static User buildUserEntity(Long id, String email) {
    return User.builder()
        .id(id)
        .name(DEFAULT_NAME)
        .surname(DEFAULT_SURNAME)
        .birthDate(DEFAULT_BIRTH_DATE)
        .email(email)
        .build();
  }

  public static UserResponseDTO buildResponseDto() {
    return UserResponseDTO.builder()
        .id(DEFAULT_ID)
        .name(DEFAULT_NAME)
        .surname(DEFAULT_SURNAME)
        .birthDate(DEFAULT_BIRTH_DATE)
        .email(DEFAULT_EMAIL)
        .build();
  }

  public static UserResponseDTO buildResponseDto(Long id, String email) {
    return UserResponseDTO.builder()
        .id(id)
        .name(DEFAULT_NAME)
        .surname(DEFAULT_SURNAME)
        .birthDate(DEFAULT_BIRTH_DATE)
        .email(email)
        .build();
  }

  public static UserUpdateRequestDTO buildUpdateRequestDto() {
    return buildUpdateRequestDto(UPDATED_NAME, UPDATED_SURNAME);
  }

  public static UserUpdateRequestDTO buildUpdateRequestDto(String name, String surname) {
    return UserUpdateRequestDTO.builder()
        .name(name)
        .surname(surname)
        .birthDate(DEFAULT_BIRTH_DATE)
        .build();
  }

  public static UserFilterDTO buildFilterDto() {
    return new UserFilterDTO(DEFAULT_NAME, DEFAULT_SURNAME);
  }

  public static UserFilterDTO buildEmptyFilterDto() {
    return new UserFilterDTO(null, null);
  }
}
