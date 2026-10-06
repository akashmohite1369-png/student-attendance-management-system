package com.attendance.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record SessionGenerateRequest(
    @NotNull Long teacherUserId,
    @NotBlank String subjectCode,
    @NotNull LocalDate sessionDate
) {}
