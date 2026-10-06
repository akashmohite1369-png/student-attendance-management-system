package com.attendance.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SessionCheckInRequest(
    @NotNull Long studentId,
    @NotBlank String code
) {}
