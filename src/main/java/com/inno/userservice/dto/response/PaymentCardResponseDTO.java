package com.inno.userservice.dto.response;

import java.time.OffsetDateTime;
import lombok.Builder;

@Builder
public record PaymentCardResponseDTO(
    Long id,
    String number,
    String holder,
    String expirationDate,
    Boolean active,
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt) {}
