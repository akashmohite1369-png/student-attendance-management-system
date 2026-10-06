package com.attendance.dto;

import com.attendance.entity.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
    @NotBlank @Size(min = 2, max = 120) String name,
    @Size(max = 30) String rollNumber,
    @Email @NotBlank @Size(max = 180) String email,
    @NotBlank @Size(min = 6, max = 72) String password,
    @NotBlank @Size(min = 6, max = 72) String confirmPassword,
    @NotNull Role role
) {}
