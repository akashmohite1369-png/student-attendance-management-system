package com.attendance.repository;
import com.attendance.entity.AttendanceRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
public interface AttendanceRepository extends JpaRepository<AttendanceRecord, Long> {
    List<AttendanceRecord> findByStudentIdOrderByAttendanceDateDesc(Long studentId);
    List<AttendanceRecord> findBySubjectCodeIgnoreCaseOrderByStudentName(String subjectCode);
    List<AttendanceRecord> findBySubjectCodeIgnoreCaseAndAttendanceDate(String subjectCode, LocalDate date);
    Optional<AttendanceRecord> findByStudentIdAndSubjectIdAndAttendanceDate(Long studentId, Long subjectId, LocalDate date);
}
