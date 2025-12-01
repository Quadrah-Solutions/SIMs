package com.quadrah.sims.repository;

import com.quadrah.sims.model.Allergy;
import com.quadrah.sims.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AllergyRepository extends JpaRepository<Allergy, Long> {

    // Find all allergies for a student
    List<Allergy> findByStudentOrderByAllergyType(Student student);

    // Find allergies by type (across all students)
    List<Allergy> findByAllergyTypeContainingIgnoreCase(String allergyType);

    // Find students with specific allergy type
    @Query("SELECT a.student FROM Allergy a WHERE LOWER(a.allergyType) LIKE LOWER(CONCAT('%', :allergyType, '%'))")
    List<Student> findStudentsByAllergyType(@Param("allergyType") String allergyType);

    // Count allergies by type for reporting
    @Query("SELECT a.allergyType, COUNT(a) FROM Allergy a GROUP BY a.allergyType")
    List<Object[]> countAllergiesByType();

    // Check if student has a specific allergy
    boolean existsByStudentAndAllergyTypeContainingIgnoreCase(Student student, String allergyType);
}
