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
        User user = users.findByEmailIgnoreCase(request.email())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password"));
        Role requestedRole;
        try { requestedRole = Role.valueOf(request.role().toUpperCase()); }
        catch (Exception ex) { throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid role"); }
        if (user.getRole() != requestedRole || !encoder.matches(request.password(), user.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email, password or role");
        }
        return userResponse(user, null);
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Object> register(@Valid @RequestBody RegisterRequest request) {
        if (!request.password().equals(request.confirmPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Passwords do not match");
        }
        String email = request.email().trim().toLowerCase();
        if (users.findByEmailIgnoreCase(email).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "An account with this email already exists");
        }

        if (request.role() == Role.STUDENT) {
            if (request.rollNumber() == null || request.rollNumber().isBlank()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Roll number is required for students");
            }
            String roll = request.rollNumber().trim();
            Student existingByEmail = students.findByEmailIgnoreCase(email).orElse(null);
            if (existingByEmail != null) {
                if (!existingByEmail.getRollNumber().equalsIgnoreCase(roll)) {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "This email is already linked to a different roll number");
                }
                User user = users.save(new User(email, encoder.encode(request.password()), Role.STUDENT, existingByEmail.getName()));
                return userResponse(user, existingByEmail.getId());
            }
            if (students.findAll().stream().anyMatch(s -> s.getRollNumber().equalsIgnoreCase(roll))) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "This roll number is already registered");
            }
            Student student = students.save(new Student(roll, request.name().trim(), email));
            User user = users.save(new User(email, encoder.encode(request.password()), Role.STUDENT, request.name().trim()));
            return userResponse(user, student.getId());
        }

        User user = users.save(new User(email, encoder.encode(request.password()), Role.TEACHER, request.name().trim()));
        return userResponse(user, null);
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
