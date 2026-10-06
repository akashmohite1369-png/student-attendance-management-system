package com.attendance.dto;

import com.attendance.entity.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record RegisterRequest(
    @NotBlank String name,
    String rollNumber,
    @Email @NotBlank String email,
    @NotBlank String password,
    @NotBlank String confirmPassword,
    @NotNull Role role
) {}
