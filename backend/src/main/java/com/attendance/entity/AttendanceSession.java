package com.attendance.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "attendance_sessions", uniqueConstraints = @UniqueConstraint(columnNames = {"code"}))
public class AttendanceSession {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "teacher_user_id", nullable = false)
    private Long teacherUserId;

    @Column(name = "subject_code", nullable = false, length = 20)
    private String subjectCode;

    @Column(name = "session_date", nullable = false)
    private LocalDate sessionDate;

    @Column(nullable = false, unique = true, length = 16)
    private String code;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    protected AttendanceSession() {}

    public AttendanceSession(Long teacherUserId, String subjectCode, LocalDate sessionDate, String code, LocalDateTime expiresAt) {
        this.teacherUserId = teacherUserId;
        this.subjectCode = subjectCode;
        this.sessionDate = sessionDate;
        this.code = code;
        this.expiresAt = expiresAt;
    }
    public Long getId() { return id; }
    public Long getTeacherUserId() { return teacherUserId; }
    public String getSubjectCode() { return subjectCode; }
    public LocalDate getSessionDate() { return sessionDate; }
    public String getCode() { return code; }
    public LocalDateTime getExpiresAt() { return expiresAt; }
}
