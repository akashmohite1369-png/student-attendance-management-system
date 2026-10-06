package com.attendance.repository;
import com.attendance.entity.Subject;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
public interface SubjectRepository extends JpaRepository<Subject, Long> {
    Optional<Subject> findByCodeIgnoreCase(String code);
    java.util.List<Subject> findAllByOrderByIdAsc();
}
