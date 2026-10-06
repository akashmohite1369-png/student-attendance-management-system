package com.attendance.controller;

import com.attendance.dto.LoginRequest;
import com.attendance.dto.RegisterRequest;
import com.attendance.entity.Role;
import com.attendance.entity.Student;
import com.attendance.entity.User;
import com.attendance.repository.StudentRepository;
import com.attendance.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {
    private final UserRepository users;
    private final StudentRepository students;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public AuthController(UserRepository users, StudentRepository students) {
        this.users = users;
        this.students = students;
    }

    @PostMapping("/login")
    public Map<String, Object> login(@Valid @RequestBody LoginRequest request) {
        String email = request.email().trim();
        User user = users.findByEmailIgnoreCase(email)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password"));

        Role requestedRole;
        try {
            requestedRole = Role.valueOf(request.role().trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid role");
        }

        if (user.getRole() != requestedRole || !encoder.matches(request.password(), user.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email, password or role");
        }

        return userResponse(user, studentIdFor(user));
    }

    @PostMapping("/register")
    @Transactional
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Object> register(@Valid @RequestBody RegisterRequest request) {
        String email = request.email().trim().toLowerCase();
        String name = request.name().trim();

        if (!request.password().equals(request.confirmPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Passwords do not match");
        }
        if (users.findByEmailIgnoreCase(email).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "An account already exists for this email");
        }

        if (request.role() == Role.TEACHER) {
            User teacher = users.save(new User(email, encoder.encode(request.password()), Role.TEACHER, name));
            return userResponse(teacher, null);
        }

        String rollNumber = request.rollNumber() == null ? "" : request.rollNumber().trim().toUpperCase();
        if (rollNumber.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Roll number is required for a student account");
        }

        Student student = students.findByEmailIgnoreCase(email).orElse(null);
        if (student == null) {
            if (students.findByRollNumberIgnoreCase(rollNumber).isPresent()) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "That roll number is already registered");
            }
            student = students.save(new Student(rollNumber, name, email));
        }

        User user = users.save(new User(email, encoder.encode(request.password()), Role.STUDENT, student.getName()));
        return userResponse(user, student.getId());
    }

    private Long studentIdFor(User user) {
        if (user.getRole() != Role.STUDENT) return null;
        return students.findByEmailIgnoreCase(user.getEmail()).map(Student::getId).orElse(null);
    }

    private Map<String, Object> userResponse(User user, Long studentId) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", true);
        response.put("userId", user.getId());
        response.put("studentId", studentId);
        response.put("email", user.getEmail());
        response.put("name", user.getDisplayName());
        response.put("role", user.getRole().name());
        return response;
    }
}
