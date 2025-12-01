package com.quadrah.sims.controller;

import com.quadrah.sims.model.StudentVisit;
import com.quadrah.sims.service.StudentVisitService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/visits")
public class StudentVisitController {

    private final StudentVisitService visitService;

    public StudentVisitController(StudentVisitService visitService) {
        this.visitService = visitService;
    }

    @GetMapping
    public ResponseEntity<List<StudentVisit>> getAllVisits() {
        List<StudentVisit> visits = visitService.getAllVisits();
        return ResponseEntity.ok(visits);
    }

    @GetMapping("/{id}")
    public ResponseEntity<StudentVisit> getVisitById(@PathVariable Long id) {
        StudentVisit visit = visitService.getVisitById(id);
        return ResponseEntity.ok(visit);
    }

    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<StudentVisit>> getVisitsByStudent(@PathVariable Long studentId) {
        List<StudentVisit> visits = visitService.getVisitsByStudent(studentId);
        return ResponseEntity.ok(visits);
    }

    @GetMapping("/nurse/{nurseId}")
    public ResponseEntity<List<StudentVisit>> getVisitsByNurse(@PathVariable Long nurseId) {
        List<StudentVisit> visits = visitService.getVisitsByNurse(nurseId);
        return ResponseEntity.ok(visits);
    }

    @GetMapping("/date-range")
    public ResponseEntity<List<StudentVisit>> getVisitsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        List<StudentVisit> visits = visitService.getVisitsByDateRange(startDate, endDate);
        return ResponseEntity.ok(visits);
    }

    @GetMapping("/emergency")
    public ResponseEntity<List<StudentVisit>> getEmergencyVisits() {
        List<StudentVisit> visits = visitService.getEmergencyVisits();
        return ResponseEntity.ok(visits);
    }

    @GetMapping("/active-observations")
    public ResponseEntity<List<StudentVisit>> getActiveObservations() {
        List<StudentVisit> visits = visitService.getActiveObservations();
        return ResponseEntity.ok(visits);
    }

    @GetMapping("/recent")
    public ResponseEntity<List<StudentVisit>> getRecentVisits(@RequestParam(defaultValue = "7") int days) {
        List<StudentVisit> visits = visitService.getRecentVisits(days);
        return ResponseEntity.ok(visits);
    }

    @PostMapping
    public ResponseEntity<StudentVisit> createVisit(@Valid @RequestBody StudentVisit visit) {
        StudentVisit createdVisit = visitService.createVisit(visit);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdVisit);
    }

    @PutMapping("/{id}")
    public ResponseEntity<StudentVisit> updateVisit(@PathVariable Long id, @Valid @RequestBody StudentVisit visitDetails) {
        StudentVisit updatedVisit = visitService.updateVisit(id, visitDetails);
        return ResponseEntity.ok(updatedVisit);
    }

    @PatchMapping("/{id}/disposition")
    public ResponseEntity<StudentVisit> updateDisposition(
            @PathVariable Long id,
            @RequestParam StudentVisit.DispositionType disposition) {
        StudentVisit updatedVisit = visitService.updateDisposition(id, disposition);
        return ResponseEntity.ok(updatedVisit);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteVisit(@PathVariable Long id) {
        visitService.deleteVisit(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/student/{studentId}/count")
    public ResponseEntity<Long> getVisitCountByStudent(@PathVariable Long studentId) {
        long count = visitService.getVisitCountByStudent(studentId);
        return ResponseEntity.ok(count);
    }
}
