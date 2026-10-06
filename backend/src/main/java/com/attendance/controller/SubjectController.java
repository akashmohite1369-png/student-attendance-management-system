package com.attendance.controller;

import com.attendance.entity.Subject;
import com.attendance.repository.SubjectRepository;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/subjects")
@CrossOrigin(origins = "*")
public class SubjectController {
    private final SubjectRepository repository;
    public SubjectController(SubjectRepository repository) { this.repository = repository; }
    @GetMapping public List<Subject> all() { return repository.findAll(); }
}
