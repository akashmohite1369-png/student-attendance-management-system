package com.attendance.repository;

import com.attendance.entity.AttendanceSession;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AttendanceSessionRepository extends JpaRepository<AttendanceSession, Long> {
    Optional<AttendanceSession> findByCodeIgnoreCase(String code);
    List<AttendanceSession> findByTeacherUserIdAndSessionDateOrderByIdDesc(Long teacherUserId, LocalDate sessionDate);
}
