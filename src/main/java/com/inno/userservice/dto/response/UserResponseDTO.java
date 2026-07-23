package com.inno.userservice.dto.response;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import lombok.Builder;

@Builder
public record UserResponseDTO(
    Long id,
    String name,
    String surname,
    LocalDate birthDate,
    String email,
    Boolean active,
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt) {}
