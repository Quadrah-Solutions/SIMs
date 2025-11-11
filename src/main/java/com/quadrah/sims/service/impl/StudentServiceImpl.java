package com.quadrah.sims.service.impl;

import com.quadrah.sims.exceptions.DuplicateResourceException;
import com.quadrah.sims.exceptions.ResourceNotFoundException;
import com.quadrah.sims.model.dto.request.EmergencyContactDTO;
import com.quadrah.sims.model.dto.request.StudentRequestDTO;
import com.quadrah.sims.model.dto.response.StudentResponseDTO;
import com.quadrah.sims.model.entity.EmergencyContact;
import com.quadrah.sims.model.entity.Student;
import com.quadrah.sims.repository.StudentRepository;
import com.quadrah.sims.service.StudentService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class StudentServiceImpl implements StudentService {

    private final StudentRepository studentRepository;

    public StudentServiceImpl(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }


    @Override
    public StudentResponseDTO createStudent(StudentRequestDTO dto) {
        if (studentRepository.existsByStudentId(dto.studentId())) {
            throw new DuplicateResourceException("Student with ID " + dto.studentId() + " already exists");
        }

        Student student = mapToEntity(dto);
        Student savedStudent = studentRepository.save(student);

        return mapToDTO(savedStudent);
    }

    @Override
    public StudentResponseDTO getStudentById(String studentId) {
        Student student = studentRepository.findByStudentId(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student with ID " + studentId + " not found"));
        return mapToDTO(student);
    }

    @Override
    public Page<StudentResponseDTO> getAllStudents(int page, int size) {
        Page<Student> studentPage = studentRepository.findAll(PageRequest.of(page, size));
        List<StudentResponseDTO> dtoList = studentPage.getContent()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
        return new PageImpl<>(dtoList, studentPage.getPageable(), studentPage.getTotalElements());
    }

    @Override
    public StudentResponseDTO updateStudent(String studentId, StudentRequestDTO dto) {
        Student student = studentRepository.findByStudentId(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student with ID " + studentId + " not found"));

        student.setStudentId(dto.studentId());
        student.setFirstName(dto.firstName());
        student.setLastName(dto.lastName());
        student.setGradeLevel(dto.gradeLevel());
        student.setHomeroom(dto.homeroom());
        student.setDateOfBirth(dto.dateOfBirth());
        student.setGender(dto.gender());
        student.setSpecialNotes(dto.specialNotes());

        // Update emergency contacts
        List<EmergencyContact> contacts = dto.emergencyContacts().stream()
                .map(ecDto -> {
                    EmergencyContact ec = new EmergencyContact();
                    ec.setContactName(ecDto.contactName());
                    ec.setPhoneNumber(ecDto.phoneNumber());
                    ec.setStudent(student);
                    return ec;
                }).toList();

        student.getEmergencyContacts().clear();
        student.getEmergencyContacts().addAll(contacts);

        Student updatedStudent = studentRepository.save(student);
        return mapToDTO(updatedStudent);
    }

    @Override
    public void deleteStudent(String studentId) {
        Student student = studentRepository.findByStudentId(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student with ID " + studentId + " not found"));
        studentRepository.delete(student);
    }

    // ----------------- Manual Mapping -----------------
    private Student mapToEntity(StudentRequestDTO dto) {
        Student student = new Student();
        student.setStudentId(dto.studentId());
        student.setFirstName(dto.firstName());
        student.setLastName(dto.lastName());
        student.setGradeLevel(dto.gradeLevel());
        student.setHomeroom(dto.homeroom());
        student.setDateOfBirth(dto.dateOfBirth());
        student.setGender(dto.gender());
        student.setSpecialNotes(dto.specialNotes());

        List<EmergencyContact> contacts = dto.emergencyContacts().stream()
                .map(ecDto -> {
                    EmergencyContact ec = new EmergencyContact();
                    ec.setContactName(ecDto.contactName());
                    ec.setPhoneNumber(ecDto.phoneNumber());
                    ec.setStudent(student);
                    return ec;
                }).toList();

        student.setEmergencyContacts(contacts);
        return student;
    }

    private StudentResponseDTO mapToDTO(Student student) {
        List<EmergencyContactDTO> emergencyContacts = student.getEmergencyContacts().stream()
                .map(ec -> new EmergencyContactDTO(
                        ec.getContactName(),
                        ec.getPhoneNumber()
                )).toList();

        return new StudentResponseDTO(
                student.getStudentId(),
                student.getFirstName(),
                student.getLastName(),
                student.getGradeLevel(),
                student.getHomeroom(),
                student.getDateOfBirth(),
                student.getGender(),
                student.getSpecialNotes(),
                emergencyContacts
        );
    }
}
