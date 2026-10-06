package com.attendance.dto;

import com.attendance.entity.AttendanceStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;

public record AttendanceRequest(
    @NotNull Long studentId,
    @NotBlank String subjectCode,
    @NotNull LocalDate attendanceDate,
    @NotNull AttendanceStatus status
) {}
