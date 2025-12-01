package com.quadrah.sims.service;

import com.quadrah.sims.model.Student;
import com.quadrah.sims.repository.StudentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class StudentService {

    private final StudentRepository studentRepository;

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
