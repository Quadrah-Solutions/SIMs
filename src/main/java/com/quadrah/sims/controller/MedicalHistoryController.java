package com.quadrah.sims.controller;

import com.quadrah.sims.model.MedicalHistory;
import com.quadrah.sims.model.Student;
import com.quadrah.sims.service.MedicalHistoryService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/medical-history")
public class MedicalHistoryController {

    private final MedicalHistoryService medicalHistoryService;

    public MedicalHistoryController(MedicalHistoryService medicalHistoryService) {
        this.medicalHistoryService = medicalHistoryService;
    }

    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<MedicalHistory>> getMedicalHistoryByStudent(@PathVariable Long studentId) {
        List<MedicalHistory> medicalHistory = medicalHistoryService.getMedicalHistoryByStudent(studentId);
        return ResponseEntity.ok(medicalHistory);
    }

    @PostMapping("/student/{studentId}")
    public ResponseEntity<MedicalHistory> createMedicalHistory(
            @PathVariable Long studentId,
            @Valid @RequestBody MedicalHistory medicalHistory) {
        MedicalHistory createdMedicalHistory = medicalHistoryService.createMedicalHistory(studentId, medicalHistory);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdMedicalHistory);
    }

    @PutMapping("/{medicalHistoryId}")
    public ResponseEntity<MedicalHistory> updateMedicalHistory(
            @PathVariable Long medicalHistoryId,
            @Valid @RequestBody MedicalHistory medicalHistoryDetails) {
        MedicalHistory updatedMedicalHistory = medicalHistoryService.updateMedicalHistory(medicalHistoryId, medicalHistoryDetails);
        return ResponseEntity.ok(updatedMedicalHistory);
    }

    @DeleteMapping("/{medicalHistoryId}")
    public ResponseEntity<Void> deactivateMedicalHistory(@PathVariable Long medicalHistoryId) {
        medicalHistoryService.deactivateMedicalHistory(medicalHistoryId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/check")
    public ResponseEntity<Boolean> checkStudentHasCondition(
            @RequestParam Long studentId,
            @RequestParam String conditionName) {
        boolean hasCondition = medicalHistoryService.studentHasCondition(studentId, conditionName);
        return ResponseEntity.ok(hasCondition);
    }

    @GetMapping("/students-with-condition")
    public ResponseEntity<List<Student>> getStudentsWithCondition(@RequestParam String conditionName) {
        List<Student> students = medicalHistoryService.getStudentsWithCondition(conditionName);
        return ResponseEntity.ok(students);
    }

    // NEW ENDPOINT: Medical History Summary Report
    @GetMapping("/summary")
    public ResponseEntity<Map<String, Object>> getMedicalHistorySummary(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) ZonedDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) ZonedDateTime endDate) {

        // Validate date range
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Start date must be before end date");
        }

        Map<String, Object> summary = medicalHistoryService.getMedicalHistorySummary(startDate, endDate);
        return ResponseEntity.ok(summary);
    }
}