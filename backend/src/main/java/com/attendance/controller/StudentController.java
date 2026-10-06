package com.attendance.controller;

import com.attendance.entity.Student;
import com.attendance.repository.StudentRepository;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/students")
@CrossOrigin(origins = "*")
public class StudentController {
    private final StudentRepository repository;
    public StudentController(StudentRepository repository) { this.repository = repository; }
    @GetMapping public List<Student> all() { return repository.findAll(); }
}
