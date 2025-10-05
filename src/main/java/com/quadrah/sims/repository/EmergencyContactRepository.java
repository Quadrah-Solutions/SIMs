package com.quadrah.sims.repository;

import com.quadrah.sims.model.EmergencyContact;
import com.quadrah.sims.model.Student;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmergencyContactRepository extends JpaRepository<EmergencyContact, Long> {

    // Find all emergency contacts for a student
    List<EmergencyContact> findByStudentOrderByIsPrimaryDesc(Student student);

    // Find primary emergency contact for a student
    Optional<EmergencyContact> findByStudentAndIsPrimaryTrue(Student student);

    // Find emergency contacts by phone number (useful for quick lookup)
    List<EmergencyContact> findByPhoneNumber(String phoneNumber);

    // Check if student has any emergency contacts
    boolean existsByStudent(Student student);

    // Count primary contacts (for validation reports)
    long countByIsPrimaryTrue();
}
