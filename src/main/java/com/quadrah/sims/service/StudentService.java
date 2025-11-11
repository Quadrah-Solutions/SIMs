package com.quadrah.sims.service;

import com.quadrah.sims.model.dto.request.StudentRequestDTO;
import com.quadrah.sims.model.dto.response.StudentResponseDTO;
import org.springframework.data.domain.Page;

import java.util.List;

public interface StudentService {

    StudentResponseDTO createStudent(StudentRequestDTO studentRequest);
    StudentResponseDTO getStudentById(String studentId);

    Page<StudentResponseDTO> getAllStudents(int page, int size);

    StudentResponseDTO updateStudent(String studentId, StudentRequestDTO studentRequest);

    void deleteStudent(String studentId);
}

