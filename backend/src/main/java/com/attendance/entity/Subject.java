package com.attendance.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "subjects")
public class Subject {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true, length = 20)
    private String code;
    @Column(nullable = false, length = 160)
    private String name;

    protected Subject() {}
    public Subject(String code, String name) { this.code = code; this.name = name; }
    public Long getId() { return id; }
    public String getCode() { return code; }
    public String getName() { return name; }
    public void setCode(String code) { this.code = code; }
    public void setName(String name) { this.name = name; }
}
