package com.attendance.controller;

import com.attendance.dto.LoginRequest;
import com.attendance.entity.Role;
import com.attendance.entity.User;
import com.attendance.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {
    private final UserRepository users;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public AuthController(UserRepository users) {
        this.users = users;
    }

    @PostMapping("/login")
    public Map<String, Object> login(@Valid @RequestBody LoginRequest request) {
        User user = users.findByEmailIgnoreCase(request.email())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password"));

        Role requestedRole;
        try {
            requestedRole = Role.valueOf(request.role().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid role");
        }

        if (user.getRole() != requestedRole || !encoder.matches(request.password(), user.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email, password or role");
        }

        Map<String, Object> response = new java.util.LinkedHashMap<>();
        response.put("success", true);
        response.put("userId", user.getId());
        response.put("email", user.getEmail());
        response.put("name", user.getDisplayName());
        response.put("role", user.getRole().name());
        return response;
    }
}
