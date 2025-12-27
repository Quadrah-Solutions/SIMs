package com.quadrah.sims.service;

import com.quadrah.sims.dto.StudentDTO;
import com.quadrah.sims.exception.ResourceNotFoundException;
import com.quadrah.sims.model.Allergy;
import com.quadrah.sims.model.EmergencyContact;
import com.quadrah.sims.model.Student;
import com.quadrah.sims.repository.StudentRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class StudentService {

    private final StudentRepository studentRepository;

    // Add this method for filtering with pagination
    public Page<StudentDTO> getStudentsWithFilters(
            Pageable pageable,
            String search,
            String grade,
            String className) {

        Specification<Student> spec = Specification.where(null);

        // Search filter (search in firstName, lastName, studentId)
        if (search != null && !search.isEmpty()) {
            spec = spec.and((root, query, cb) ->
                    cb.or(
                            cb.like(cb.lower(root.get("firstName")), "%" + search.toLowerCase() + "%"),
                            cb.like(cb.lower(root.get("lastName")), "%" + search.toLowerCase() + "%"),
                            cb.like(cb.lower(root.get("studentId")), "%" + search.toLowerCase() + "%")
                    )
            );
        }

        // Grade filter
        if (grade != null && !grade.isEmpty()) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("gradeLevel"), grade)
            );
        }

        // Class filter (using homeroom)
        if (className != null && !className.isEmpty()) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("homeroom"), className)
            );
        }

        // Apply pagination and sorting
        Page<Student> studentsPage = studentRepository.findAll(spec, pageable);

        // Convert to DTOs
        List<StudentDTO> studentDTOs = studentsPage.getContent().stream()
                .map(StudentDTO::new)
                .collect(Collectors.toList());

        return new PageImpl<>(studentDTOs, pageable, studentsPage.getTotalElements());
    }

    public List<StudentDTO> getAllStudentsDTO() {
        return studentRepository.findAll().stream()
                .map(StudentDTO::new)
                .collect(Collectors.toList());
    }



    public StudentService(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    public List<Student> getAllStudents() {
        return studentRepository.findAllByOrderByLastNameAscFirstNameAsc();
    }

    public Optional<Student> getStudentById(Long id) {
        return studentRepository.findById(id);
    }

    public Optional<Student> getStudentByStudentId(String studentId) {
        return studentRepository.findByStudentId(studentId);
    }

    public List<Student> getStudentsByGradeLevel(String gradeLevel) {
        return studentRepository.findByGradeLevel(gradeLevel);
    }

    public List<Student> getStudentsByHomeroom(String homeroom) {
        return studentRepository.findByHomeroom(homeroom);
    }

    public List<Student> searchStudentsByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return getAllStudents();
        }
        return studentRepository.findByNameContainingIgnoreCase(name.trim());
    }

    public Student createStudent(Student student) {
        validateStudent(student);

        // Check if student ID already exists
        if (studentRepository.existsByStudentId(student.getStudentId())) {
            throw new IllegalArgumentException("Student with ID " + student.getStudentId() + " already exists.");
        }

        // Set student reference on each emergency contact
        if (student.getEmergencyContacts() != null) {
            for (EmergencyContact contact : student.getEmergencyContacts()) {
                contact.setStudent(student); // Set the bidirectional relationship

                // If isPrimary is null, set default to false
                if (contact.getIsPrimary() == null) {
                    contact.setIsPrimary(false);
                }
            }
        }

        // Set student reference on each allergy
        if (student.getAllergies() != null) {
            for (Allergy allergy : student.getAllergies()) {
                allergy.setStudent(student); // Set the bidirectional relationship
            }
        }

        // CascadeType.ALL should handle saving the related entities
        return studentRepository.save(student);
    }

    public Student updateStudent(Long id, Student studentDetails) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Student not found with id: " + id));

        validateStudent(studentDetails);

        // Check if student ID is being changed to an existing one
        if (!student.getStudentId().equals(studentDetails.getStudentId()) &&
                studentRepository.existsByStudentId(studentDetails.getStudentId())) {
            throw new IllegalArgumentException("Student with ID " + studentDetails.getStudentId() + " already exists.");
        }

        student.setStudentId(studentDetails.getStudentId());
        student.setFirstName(studentDetails.getFirstName());
        student.setLastName(studentDetails.getLastName());
        student.setGradeLevel(studentDetails.getGradeLevel());
        student.setHomeroom(studentDetails.getHomeroom());
        student.setDateOfBirth(studentDetails.getDateOfBirth());
        student.setGender(studentDetails.getGender());
        student.setSpecialNotes(studentDetails.getSpecialNotes());

        return studentRepository.save(student);
    }

    public void deleteStudent(Long id) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Student not found with id: " + id));

        // Check if student has visits (optional business rule)
        // if (!student.getVisits().isEmpty()) {
        //     throw new IllegalStateException("Cannot delete student with existing visit records.");
        // }

        studentRepository.delete(student);
    }

    public boolean studentExists(String studentId) {
        return studentRepository.existsByStudentId(studentId);
    }

    private void validateStudent(Student student) {
        if (student.getStudentId() == null || student.getStudentId().trim().isEmpty()) {
            throw new IllegalArgumentException("Student ID is required.");
        }
        if (student.getFirstName() == null || student.getFirstName().trim().isEmpty()) {
            throw new IllegalArgumentException("First name is required.");
        }
        if (student.getLastName() == null || student.getLastName().trim().isEmpty()) {
            throw new IllegalArgumentException("Last name is required.");
        }
    }
}
