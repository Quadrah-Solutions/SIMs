package com.quadrah.sims.repository;


import com.quadrah.sims.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {

    // Find student by student ID (unique identifier)
    Optional<Student> findByStudentId(String studentId);

    // Find students by grade level
    List<Student> findByGradeLevel(String gradeLevel);

    // Find students by homeroom
    List<Student> findByHomeroom(String homeroom);

    // Search students by name (first or last name containing search term)
    @Query("SELECT s FROM Student s WHERE LOWER(s.firstName) LIKE LOWER(CONCAT('%', :name, '%')) OR LOWER(s.lastName) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<Student> findByNameContainingIgnoreCase(@Param("name") String name);

    // Find students by grade level and homeroom
    List<Student> findByGradeLevelAndHomeroom(String gradeLevel, String homeroom);

    // Check if student ID already exists (for validation)
    boolean existsByStudentId(String studentId);

    // Find all students ordered by last name, first name
    List<Student> findAllByOrderByLastNameAscFirstNameAsc();
}