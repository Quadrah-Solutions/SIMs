package com.quadrah.sims.controller;

import com.quadrah.sims.model.MedicationAdministration;
import com.quadrah.sims.model.MedicationInventory;
import com.quadrah.sims.service.MedicationService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/medications")
public class MedicationController {

    private final MedicationService medicationService;

    public MedicationController(MedicationService medicationService) {
        this.medicationService = medicationService;
    }

    // Medication Inventory Endpoints
    @GetMapping("/inventory")
    public ResponseEntity<List<MedicationInventory>> getAllMedications() {
        List<MedicationInventory> medications = medicationService.getAllMedications();
        return ResponseEntity.ok(medications);
    }

    @GetMapping("/inventory/{id}")
    public ResponseEntity<MedicationInventory> getMedicationById(@PathVariable Long id) {
        MedicationInventory medication = medicationService.getMedicationById(id);
        return ResponseEntity.ok(medication);
    }

    @GetMapping("/inventory/low-stock")
    public ResponseEntity<List<MedicationInventory>> getLowStockMedications() {
        List<MedicationInventory> medications = medicationService.getLowStockMedications();
        return ResponseEntity.ok(medications);
    }

    @GetMapping("/inventory/expiring")
    public ResponseEntity<List<MedicationInventory>> getExpiringMedications(
            @RequestParam(defaultValue = "30") int daysThreshold) {
        List<MedicationInventory> medications = medicationService.getExpiringMedications(daysThreshold);
        return ResponseEntity.ok(medications);
    }

    @PostMapping("/inventory")
    public ResponseEntity<MedicationInventory> createMedication(@Valid @RequestBody MedicationInventory medication) {
        MedicationInventory createdMedication = medicationService.createMedication(medication);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdMedication);
    }

    @PutMapping("/inventory/{id}")
    public ResponseEntity<MedicationInventory> updateMedication(
            @PathVariable Long id,
            @Valid @RequestBody MedicationInventory medicationDetails) {
        MedicationInventory updatedMedication = medicationService.updateMedication(id, medicationDetails);
        return ResponseEntity.ok(updatedMedication);
    }

    @PatchMapping("/inventory/{id}/stock")
    public ResponseEntity<MedicationInventory> updateStock(
            @PathVariable Long id,
            @RequestParam Integer quantityChange,
            @RequestParam(defaultValue = "Manual adjustment") String reason) {
        MedicationInventory updatedMedication = medicationService.updateStock(id, quantityChange, reason);
        return ResponseEntity.ok(updatedMedication);
    }

    @DeleteMapping("/inventory/{id}")
    public ResponseEntity<Void> deactivateMedication(@PathVariable Long id) {
        medicationService.deactivateMedication(id);
        return ResponseEntity.noContent().build();
    }

    // Medication Administration Endpoints
    @PostMapping("/administration")
    public ResponseEntity<MedicationAdministration> administerMedication(
            @Valid @RequestBody MedicationAdministration administration) {
        MedicationAdministration savedAdministration = medicationService.administerMedication(administration);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedAdministration);
    }

    @GetMapping("/administration/visit/{visitId}")
    public ResponseEntity<List<MedicationAdministration>> getAdministrationsByVisit(@PathVariable Long visitId) {
        List<MedicationAdministration> administrations = medicationService.getMedicationAdministrationsByVisit(visitId);
        return ResponseEntity.ok(administrations);
    }

    @GetMapping("/administration/student/{studentId}")
    public ResponseEntity<List<MedicationAdministration>> getAdministrationsByStudent(@PathVariable Long studentId) {
        List<MedicationAdministration> administrations = medicationService.getMedicationAdministrationsByStudent(studentId);
        return ResponseEntity.ok(administrations);
    }

    @GetMapping("/administration/recent")
    public ResponseEntity<List<MedicationAdministration>> getRecentAdministrations() {
        List<MedicationAdministration> administrations = medicationService.getRecentMedicationAdministrations();
        return ResponseEntity.ok(administrations);
    }
}
