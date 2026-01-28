package com.quadrah.sims.controller;

import com.quadrah.sims.model.Allergy;
import com.quadrah.sims.model.Student;
import com.quadrah.sims.service.AllergyService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/allergies")
public class AllergyController {

    private final AllergyService allergyService;

    public AllergyController(AllergyService allergyService) {
        this.allergyService = allergyService;
    }

    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<Allergy>> getAllergiesByStudent(@PathVariable Long studentId) {
        List<Allergy> allergies = allergyService.getAllergiesByStudent(studentId);
        return ResponseEntity.ok(allergies);
    }

    @PostMapping("/student/{studentId}")
    public ResponseEntity<Allergy> createAllergy(
            @PathVariable Long studentId,
            @Valid @RequestBody Allergy allergy) {
        Allergy createdAllergy = allergyService.createAllergy(studentId, allergy);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdAllergy);
    }

    @PutMapping("/{allergyId}")
    public ResponseEntity<Allergy> updateAllergy(
            @PathVariable Long allergyId,
            @Valid @RequestBody Allergy allergyDetails) {
        Allergy updatedAllergy = allergyService.updateAllergy(allergyId, allergyDetails);
        return ResponseEntity.ok(updatedAllergy);
    }

    @DeleteMapping("/{allergyId}")
    public ResponseEntity<Void> deleteAllergy(@PathVariable Long allergyId) {
        allergyService.deleteAllergy(allergyId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/check")
    public ResponseEntity<Boolean> checkStudentHasAllergy(
            @RequestParam Long studentId,
            @RequestParam String allergyType) {
        boolean hasAllergy = allergyService.studentHasAllergy(studentId, allergyType);
        return ResponseEntity.ok(hasAllergy);
    }

    @GetMapping("/students-with-allergy")
    public ResponseEntity<List<Student>> getStudentsWithAllergy(@RequestParam String allergyType) {
        List<Student> students = allergyService.getStudentsWithAllergy(allergyType);
        return ResponseEntity.ok(students);
    }
}
