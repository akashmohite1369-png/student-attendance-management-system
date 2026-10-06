package com.attendance.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "students")
public class Student {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 30, unique = true)
    private String rollNumber;
    @Column(nullable = false, length = 120)
    private String name;
    @Column(nullable = false, unique = true, length = 180)
    private String email;

    protected Student() {}
    public Student(String rollNumber, String name, String email) {
        this.rollNumber = rollNumber; this.name = name; this.email = email;
    }
    public Long getId() { return id; }
    public String getRollNumber() { return rollNumber; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public void setRollNumber(String rollNumber) { this.rollNumber = rollNumber; }
    public void setName(String name) { this.name = name; }
    public void setEmail(String email) { this.email = email; }
}
