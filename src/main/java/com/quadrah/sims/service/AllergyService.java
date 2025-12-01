package com.quadrah.sims.service;

import com.quadrah.sims.model.Allergy;
import com.quadrah.sims.model.Student;
import com.quadrah.sims.repository.AllergyRepository;
import com.quadrah.sims.repository.StudentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class AllergyService {

    private final AllergyRepository allergyRepository;
    private final StudentRepository studentRepository;

    public AllergyService(AllergyRepository allergyRepository, StudentRepository studentRepository) {
        this.allergyRepository = allergyRepository;
        this.studentRepository = studentRepository;
    }

    public List<Allergy> getAllergiesByStudent(Long studentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found with id: " + studentId));
        return allergyRepository.findByStudentOrderByAllergyType(student);
    }

    public Allergy createAllergy(Long studentId, Allergy allergy) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found with id: " + studentId));

        validateAllergy(allergy);

        allergy.setStudent(student);
        return allergyRepository.save(allergy);
    }

    public Allergy updateAllergy(Long allergyId, Allergy allergyDetails) {
        Allergy allergy = allergyRepository.findById(allergyId)
                .orElseThrow(() -> new IllegalArgumentException("Allergy not found with id: " + allergyId));

        validateAllergy(allergyDetails);

        allergy.setAllergyType(allergyDetails.getAllergyType());
        allergy.setSeverity(allergyDetails.getSeverity());
        allergy.setReaction(allergyDetails.getReaction());
        allergy.setNotes(allergyDetails.getNotes());

        return allergyRepository.save(allergy);
    }

    public void deleteAllergy(Long allergyId) {
        Allergy allergy = allergyRepository.findById(allergyId)
                .orElseThrow(() -> new IllegalArgumentException("Allergy not found with id: " + allergyId));

        allergyRepository.delete(allergy);
    }

    public boolean studentHasAllergy(Long studentId, String allergyType) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found with id: " + studentId));
        return allergyRepository.existsByStudentAndAllergyTypeContainingIgnoreCase(student, allergyType);
    }

    public List<Student> getStudentsWithAllergy(String allergyType) {
        return allergyRepository.findStudentsByAllergyType(allergyType);
    }

    private void validateAllergy(Allergy allergy) {
        if (allergy.getAllergyType() == null || allergy.getAllergyType().trim().isEmpty()) {
            throw new IllegalArgumentException("Allergy type is required.");
        }
    }
}
