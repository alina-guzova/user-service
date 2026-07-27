package com.inno.userservice.dto.request;

import jakarta.validation.constraints.*;
import lombok.Builder;

@Builder
public record PaymentCardUpdateRequestDTO(
    @NotBlank(message = "Field 'holder' is required and must not be empty")
        @Size(max = 255, message = "Max length of field 'holder' = 255")
        String holder,
    @NotBlank(message = "Field 'expirationDate' is required and must not be empty")
        @Pattern(
            regexp = "^(0[1-9]|1[0-2])\\/([0-9]{2})$",
            message = "Field 'expirationDate' must match pattern 'MM/yy'")
        String expirationDate) {}
