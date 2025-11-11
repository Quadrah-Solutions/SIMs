package com.quadrah.sims.controller;


import com.quadrah.sims.model.dto.request.StudentRequestDTO;
import com.quadrah.sims.model.dto.response.ApiResponse;
import com.quadrah.sims.model.dto.response.StudentResponseDTO;
import com.quadrah.sims.service.StudentService;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/students")
public class StudentController {

    private final StudentService studentService;

    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }


    // ---------------- Create Student ----------------
    @PostMapping
    @ResponseStatus(org.springframework.http.HttpStatus.CREATED)
    public ApiResponse<StudentResponseDTO> createStudent(@RequestBody StudentRequestDTO studentRequest) {
        StudentResponseDTO created = studentService.createStudent(studentRequest);
        return new ApiResponse<>(
                true,
                "Student created successfully",
                created,
                LocalDateTime.now()
        );
    }

    // ---------------- Get Student by ID ----------------
    @GetMapping("/{studentId}")
    public ApiResponse<StudentResponseDTO> getStudentById(@PathVariable String studentId) {
        StudentResponseDTO student = studentService.getStudentById(studentId);
        return new ApiResponse<>(
                true,
                "Student retrieved successfully",
                student,
                LocalDateTime.now()
        );
    }

    // ---------------- Get All Students with Pagination ----------------
    @GetMapping
    public ApiResponse<Page<StudentResponseDTO>> getAllStudents(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Page<StudentResponseDTO> students = studentService.getAllStudents(page, size);
        return new ApiResponse<>(
                true,
                "Students retrieved successfully",
                students,
                LocalDateTime.now()
        );
    }

    // ---------------- Update Student ----------------
    @PutMapping("/{studentId}")
    public ApiResponse<StudentResponseDTO> updateStudent(
            @PathVariable String studentId,
            @RequestBody StudentRequestDTO studentRequest
    ) {
        StudentResponseDTO updated = studentService.updateStudent(studentId, studentRequest);
        return new ApiResponse<>(
                true,
                "Student updated successfully",
                updated,
                LocalDateTime.now()
        );
    }

    // ---------------- Delete Student ----------------
    @DeleteMapping("/{studentId}")
    public ApiResponse<Void> deleteStudent(@PathVariable String studentId) {
        studentService.deleteStudent(studentId);
        return new ApiResponse<>(
                true,
                "Student deleted successfully",
                null,
                LocalDateTime.now()
        );
    }
}

