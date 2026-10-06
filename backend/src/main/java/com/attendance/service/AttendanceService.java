package com.attendance.service;

import com.attendance.dto.AttendanceRequest;
import com.attendance.entity.*;
import com.attendance.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class AttendanceService {
    private final AttendanceRepository attendanceRepository;
    private final StudentRepository studentRepository;
    private final SubjectRepository subjectRepository;

    public AttendanceService(AttendanceRepository attendanceRepository, StudentRepository studentRepository, SubjectRepository subjectRepository) {
        this.attendanceRepository = attendanceRepository;
        this.studentRepository = studentRepository;
        this.subjectRepository = subjectRepository;
    }

    @Transactional
    public AttendanceRecord mark(AttendanceRequest request) {
        Student student = studentRepository.findById(request.studentId()).orElseThrow(() -> new NoSuchElementException("Student not found"));
        Subject subject = subjectRepository.findByCodeIgnoreCase(request.subjectCode()).orElseThrow(() -> new NoSuchElementException("Subject not found"));
        AttendanceRecord record = attendanceRepository.findByStudentIdAndSubjectIdAndAttendanceDate(student.getId(), subject.getId(), request.attendanceDate())
            .orElseGet(() -> new AttendanceRecord(student, subject, request.attendanceDate(), request.status()));
        record.setStatus(request.status());
        return attendanceRepository.save(record);
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> studentSummary(Long studentId) {
        Student student = studentRepository.findById(studentId).orElseThrow(() -> new NoSuchElementException("Student not found"));
        List<AttendanceRecord> records = attendanceRepository.findByStudentIdOrderByAttendanceDateDesc(studentId);
        Map<String, List<AttendanceRecord>> bySubject = new LinkedHashMap<>();
        for (Subject subject : subjectRepository.findAllByOrderByIdAsc()) bySubject.put(subject.getCode(), new ArrayList<>());
        for (AttendanceRecord record : records) bySubject.computeIfAbsent(record.getSubject().getCode(), ignored -> new ArrayList<>()).add(record);
        List<Map<String, Object>> result = new ArrayList<>();
        bySubject.forEach((code, list) -> {
            long present = list.stream().filter(r -> r.getStatus() == AttendanceStatus.PRESENT).count();
            double percentage = list.isEmpty() ? 0.0 : Math.round(present * 1000.0 / list.size()) / 10.0;
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("studentId", student.getId()); row.put("subjectCode", code); row.put("classesHeld", list.size()); row.put("classesPresent", present); row.put("percentage", percentage); row.put("eligible", !list.isEmpty() && percentage >= 75.0);
            result.add(row);
        });
        return result;
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> subjectReport(String subjectCode) {
        Subject subject = subjectRepository.findByCodeIgnoreCase(subjectCode).orElseThrow(() -> new NoSuchElementException("Subject not found"));
        List<AttendanceRecord> records = attendanceRepository.findBySubjectCodeIgnoreCaseOrderByStudentName(subject.getCode());
        Map<Long, List<AttendanceRecord>> byStudent = new LinkedHashMap<>();
        List<Student> allStudents = studentRepository.findAll();
        for (Student student : allStudents) byStudent.put(student.getId(), new ArrayList<>());
        for (AttendanceRecord record : records) byStudent.computeIfAbsent(record.getStudent().getId(), ignored -> new ArrayList<>()).add(record);
        List<Map<String, Object>> result = new ArrayList<>();
        for (Student student : allStudents) {
            List<AttendanceRecord> list = byStudent.getOrDefault(student.getId(), List.of());
            long present = list.stream().filter(r -> r.getStatus() == AttendanceStatus.PRESENT).count();
            double percentage = list.isEmpty() ? 0.0 : Math.round(present * 1000.0 / list.size()) / 10.0;
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("studentId", student.getId()); row.put("studentName", student.getName()); row.put("rollNumber", student.getRollNumber()); row.put("email", student.getEmail()); row.put("subjectCode", subject.getCode()); row.put("classesHeld", list.size()); row.put("classesPresent", present); row.put("percentage", percentage); row.put("eligible", !list.isEmpty() && percentage >= 75.0);
            result.add(row);
        }
        return result;
    }
}
