package com.quadrah.sims.repository;

import com.quadrah.sims.model.MedicalHistory;
import com.quadrah.sims.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MedicalHistoryRepository extends JpaRepository<MedicalHistory, Long> {

    // Find medical history for a student, ordered by condition name
    List<MedicalHistory> findByStudentOrderByConditionName(Student student);

    // Find active medical history for a student
    List<MedicalHistory> findByStudentAndIsActiveTrueOrderByConditionName(Student student);

    // Find medical history by condition name (across all students)
    List<MedicalHistory> findByConditionNameContainingIgnoreCase(String conditionName);

    // Find students with specific medical conditions
    @Query("SELECT m.student FROM MedicalHistory m WHERE LOWER(m.conditionName) LIKE LOWER(CONCAT('%', :conditionName, '%')) AND m.isActive = true")
    List<Student> findStudentsByConditionName(@Param("conditionName") String conditionName);

    // Count medical conditions by type for reporting
    @Query("SELECT m.conditionName, COUNT(m) FROM MedicalHistory m WHERE m.isActive = true GROUP BY m.conditionName")
    List<Object[]> countConditionsByType();

    // Check if student has a specific medical condition
    boolean existsByStudentAndConditionNameContainingIgnoreCaseAndIsActiveTrue(Student student, String conditionName);
}