package com.attendance.config;

import com.attendance.entity.Role;
import com.attendance.entity.Student;
import com.attendance.entity.Subject;
import com.attendance.entity.User;
import com.attendance.repository.StudentRepository;
import com.attendance.repository.SubjectRepository;
import com.attendance.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Configuration
public class SeedData {
    @Bean
    CommandLineRunner seed(SubjectRepository subjects, StudentRepository students, UserRepository users) {
        return args -> {
            String[][] data = {
                {"AOA", "Analysis of Algorithms"},
                {"COA", "Computer Organization & Architecture"},
                {"MATHS-3", "Mathematics-III"},
                {"DSGT", "DSGT"},
                {"FSGT", "FSGT"},
                {"ESE", "ESE"},
                {"ED", "Engineering Drawing"}
            };
            for (String[] row : data) {
                if (subjects.findByCodeIgnoreCase(row[0]).isEmpty()) {
                    subjects.save(new Subject(row[0], row[1]));
                }
            }

            if (students.count() == 0) {
                students.save(new Student("23CS001", "Aarav Patil", "student@college.edu"));
                students.save(new Student("23CS002", "Diya Shah", "diya@college.edu"));
                students.save(new Student("23CS003", "Kabir Rane", "kabir@college.edu"));
                students.save(new Student("23CS004", "Meera Joshi", "meera@college.edu"));
                students.save(new Student("23CS005", "Ishaan Kulkarni", "ishaan@college.edu"));
            }

            BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
            if (users.findByEmailIgnoreCase("student@college.edu").isEmpty()) {
                users.save(new User("student@college.edu", encoder.encode("student123"), Role.STUDENT, "Aarav Patil"));
            }
            if (users.findByEmailIgnoreCase("teacher@college.edu").isEmpty()) {
                users.save(new User("teacher@college.edu", encoder.encode("teacher123"), Role.TEACHER, "Prof. Neha Kulkarni"));
            }
        };
    }
}
