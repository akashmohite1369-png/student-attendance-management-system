package com.attendance.controller;

import com.attendance.dto.AttendanceRequest;
import com.attendance.entity.AttendanceRecord;
import com.attendance.repository.AttendanceRepository;
import com.attendance.service.AttendanceService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/attendance")
@CrossOrigin(origins = "*")
public class AttendanceController {
    private final AttendanceService service;
    private final AttendanceRepository repository;

    public AttendanceController(AttendanceService service, AttendanceRepository repository) {
        this.service = service; this.repository = repository;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Object> mark(@Valid @RequestBody AttendanceRequest request) {
        try {
            AttendanceRecord saved = service.mark(request);
            return Map.of("id", saved.getId(), "studentId", saved.getStudent().getId(),
                "subjectCode", saved.getSubject().getCode(), "attendanceDate", saved.getAttendanceDate(),
                "status", saved.getStatus());
        } catch (NoSuchElementException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage());
        }
    }

    @GetMapping("/summary")
    public List<Map<String, Object>> summary(@RequestParam Long studentId) {
        try { return service.studentSummary(studentId); }
        catch (NoSuchElementException ex) { throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage()); }
    }

    @GetMapping("/report")
    public List<Map<String, Object>> report(@RequestParam String subjectCode) {
        try {
            return service.subjectReport(subjectCode);
        } catch (NoSuchElementException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage());
        }
    }

}
