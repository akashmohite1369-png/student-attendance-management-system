package com.attendance.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "attendance_records",
       uniqueConstraints = @UniqueConstraint(columnNames = {"student_id", "subject_id", "attendance_date"}))
public class AttendanceRecord {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;
    @Column(name = "attendance_date", nullable = false)
    private LocalDate attendanceDate;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private AttendanceStatus status;

    protected AttendanceRecord() {}
    public AttendanceRecord(Student student, Subject subject, LocalDate date, AttendanceStatus status) {
        this.student = student; this.subject = subject; this.attendanceDate = date; this.status = status;
    }
    public Long getId() { return id; }
    public Student getStudent() { return student; }
    public Subject getSubject() { return subject; }
    public LocalDate getAttendanceDate() { return attendanceDate; }
    public AttendanceStatus getStatus() { return status; }
    public void setStatus(AttendanceStatus status) { this.status = status; }
}
