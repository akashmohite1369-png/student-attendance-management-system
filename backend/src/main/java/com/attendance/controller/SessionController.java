package com.attendance.controller;

import com.attendance.dto.AttendanceRequest;
import com.attendance.dto.SessionCheckInRequest;
import com.attendance.dto.SessionGenerateRequest;
import com.attendance.entity.AttendanceSession;
import com.attendance.entity.Role;
import com.attendance.entity.Student;
import com.attendance.entity.User;
import com.attendance.repository.AttendanceSessionRepository;
import com.attendance.repository.StudentRepository;
import com.attendance.repository.SubjectRepository;
import com.attendance.repository.UserRepository;
import com.attendance.service.AttendanceService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.time.*;
import java.util.*;

@RestController
@RequestMapping("/api/sessions")
@CrossOrigin(origins = "*")
public class SessionController {
    private final AttendanceSessionRepository sessions; private final UserRepository users; private final SubjectRepository subjects; private final StudentRepository students; private final AttendanceService attendanceService;
    public SessionController(AttendanceSessionRepository sessions, UserRepository users, SubjectRepository subjects, StudentRepository students, AttendanceService attendanceService) { this.sessions=sessions; this.users=users; this.subjects=subjects; this.students=students; this.attendanceService=attendanceService; }

    @PostMapping("/generate")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String,Object> generate(@Valid @RequestBody SessionGenerateRequest request) {
        User teacher = users.findById(request.teacherUserId()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,"Teacher account not found"));
        if (teacher.getRole() != Role.TEACHER) throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Only teachers can generate session codes");
        if (!request.sessionDate().equals(LocalDate.now())) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Session date must be today");
        subjects.findByCodeIgnoreCase(request.subjectCode()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,"Subject not found"));
        String code; int tries=0;
        do { code = request.subjectCode().toUpperCase(Locale.ROOT) + "-" + (1000 + new Random().nextInt(9000)); tries++; } while (sessions.findByCodeIgnoreCase(code).isPresent() && tries < 20);
        if (sessions.findByCodeIgnoreCase(code).isPresent()) throw new ResponseStatusException(HttpStatus.CONFLICT,"Could not create a unique session code");
        AttendanceSession session = sessions.save(new AttendanceSession(teacher.getId(), request.subjectCode().toUpperCase(Locale.ROOT), request.sessionDate(), code, LocalDateTime.now().plusMinutes(60)));
        return sessionMap(session);
    }

    @GetMapping("/active")
    public List<Map<String,Object>> active(@RequestParam Long teacherUserId) {
        if (!users.existsById(teacherUserId)) throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Teacher account not found");
        LocalDateTime now=LocalDateTime.now();
        return sessions.findByTeacherUserIdAndSessionDateOrderByIdDesc(teacherUserId, LocalDate.now()).stream().filter(s -> s.getExpiresAt().isAfter(now)).map(this::sessionMap).toList();
    }

    @PostMapping("/check-in")
    public Map<String,Object> checkIn(@Valid @RequestBody SessionCheckInRequest request) {
        Student student = students.findById(request.studentId()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,"Student not found"));
        AttendanceSession session = sessions.findByCodeIgnoreCase(request.code().trim()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,"Invalid session code"));
        if (!session.getSessionDate().equals(LocalDate.now()) || session.getExpiresAt().isBefore(LocalDateTime.now())) throw new ResponseStatusException(HttpStatus.GONE,"This session code has expired");
        attendanceService.mark(new AttendanceRequest(student.getId(), session.getSubjectCode(), session.getSessionDate(), com.attendance.entity.AttendanceStatus.PRESENT));
        return Map.of("success",true,"studentId",student.getId(),"subjectCode",session.getSubjectCode(),"attendanceDate",session.getSessionDate(),"message","Attendance marked present");
    }

    private Map<String,Object> sessionMap(AttendanceSession s) { return Map.of("id",s.getId(),"teacherUserId",s.getTeacherUserId(),"subjectCode",s.getSubjectCode(),"sessionDate",s.getSessionDate(),"code",s.getCode(),"expiresAt",s.getExpiresAt()); }
}
