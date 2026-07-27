package com.inno.userservice.dto.request;

import jakarta.validation.constraints.Size;

public record UserFilterDTO(
    @Size(max = 255, message = "Max length of field 'name' = 255") String name,
    @Size(max = 255, message = "Max length of field 'surname' = 255") String surname) {}
