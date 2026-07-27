package com.inno.userservice.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import lombok.Builder;

@Builder
public record UserUpdateRequestDTO(
    @NotBlank(message = "Field 'name' is required and must not be empty")
        @Size(max = 255, message = "Max length of field 'name' = 255")
        String name,
    @NotBlank(message = "Field 'surname' is required and must not be empty")
        @Size(max = 255, message = "Max length of field 'surname' = 255")
        String surname,
    @NotNull(message = "Field 'birthDate' is required") @Past @JsonFormat(pattern = "dd.MM.yyyy")
        LocalDate birthDate) {}
