package com.quadrah.sims.controller;

import com.quadrah.sims.model.MedicalHistory;
import com.quadrah.sims.model.Student;
import com.quadrah.sims.service.MedicalHistoryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/medical-history")
public class MedicalHistoryController {

    private final MedicalHistoryService medicalHistoryService;

    public MedicalHistoryController(MedicalHistoryService medicalHistoryService) {
        this.medicalHistoryService = medicalHistoryService;
    }

    @GetMapping("/student/{studentId}")
    @PreAuthorize("hasRole('NURSE') or hasRole('ADMIN')")
    public ResponseEntity<List<MedicalHistory>> getMedicalHistoryByStudent(@PathVariable Long studentId) {
        List<MedicalHistory> medicalHistory = medicalHistoryService.getMedicalHistoryByStudent(studentId);
        return ResponseEntity.ok(medicalHistory);
    }

    @PostMapping("/student/{studentId}")
    @PreAuthorize("hasRole('NURSE') or hasRole('ADMIN')")
    public ResponseEntity<MedicalHistory> createMedicalHistory(
            @PathVariable Long studentId,
            @Valid @RequestBody MedicalHistory medicalHistory) {
        MedicalHistory createdMedicalHistory = medicalHistoryService.createMedicalHistory(studentId, medicalHistory);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdMedicalHistory);
    }

    @PutMapping("/{medicalHistoryId}")
    @PreAuthorize("hasRole('NURSE') or hasRole('ADMIN')")
    public ResponseEntity<MedicalHistory> updateMedicalHistory(
            @PathVariable Long medicalHistoryId,
            @Valid @RequestBody MedicalHistory medicalHistoryDetails) {
        MedicalHistory updatedMedicalHistory = medicalHistoryService.updateMedicalHistory(medicalHistoryId, medicalHistoryDetails);
        return ResponseEntity.ok(updatedMedicalHistory);
    }

    @DeleteMapping("/{medicalHistoryId}")
    @PreAuthorize("hasRole('NURSE') or hasRole('ADMIN')")
    public ResponseEntity<Void> deactivateMedicalHistory(@PathVariable Long medicalHistoryId) {
        medicalHistoryService.deactivateMedicalHistory(medicalHistoryId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/check")
    @PreAuthorize("hasRole('NURSE') or hasRole('ADMIN')")
    public ResponseEntity<Boolean> checkStudentHasCondition(
            @RequestParam Long studentId,
            @RequestParam String conditionName) {
        boolean hasCondition = medicalHistoryService.studentHasCondition(studentId, conditionName);
        return ResponseEntity.ok(hasCondition);
    }

    @GetMapping("/students-with-condition")
    @PreAuthorize("hasRole('NURSE') or hasRole('ADMIN')")
    public ResponseEntity<List<Student>> getStudentsWithCondition(@RequestParam String conditionName) {
        List<Student> students = medicalHistoryService.getStudentsWithCondition(conditionName);
        return ResponseEntity.ok(students);
    }
}