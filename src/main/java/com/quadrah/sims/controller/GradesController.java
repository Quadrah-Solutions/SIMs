package com.quadrah.sims.controller;

import com.quadrah.sims.model.Grade;
import com.quadrah.sims.model.ClassRoom;
import com.quadrah.sims.service.GradeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("")
public class GradesController {

    private final GradeService gradeService;

    @Autowired
    public GradesController(GradeService gradeService) {
        this.gradeService = gradeService;
    }

    // Grades endpoints
    @GetMapping("/grades")
    @PreAuthorize("hasAnyRole('NURSE', 'ADMIN', 'TEACHER', 'PARENT')")
    public ResponseEntity<List<String>> getGrades() {
        List<String> grades = gradeService.getAllGradeNames();
        return ResponseEntity.ok(grades);
    }

    @GetMapping("/grades/all")
    @PreAuthorize("hasAnyRole('ADMIN', 'NURSE')")
    public ResponseEntity<List<Grade>> getAllGradesWithDetails() {
        List<Grade> grades = gradeService.getAllGrades();
        return ResponseEntity.ok(grades);
    }

    @GetMapping("/grades/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'NURSE')")
    public ResponseEntity<Grade> getGradeById(@PathVariable Long id) {
        Grade grade = gradeService.getGradeById(id);
        if (grade == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(grade);
    }

    // Classes endpoints
    @GetMapping("/classes")
    @PreAuthorize("hasAnyRole('NURSE', 'ADMIN', 'TEACHER', 'PARENT')")
    public ResponseEntity<List<String>> getClasses() {
        List<String> classes = gradeService.getAllClassNames();
        return ResponseEntity.ok(classes);
    }

    @GetMapping("/classes/all")
    @PreAuthorize("hasAnyRole('ADMIN', 'NURSE')")
    public ResponseEntity<List<ClassRoom>> getAllClassRooms() {
        List<ClassRoom> classRooms = gradeService.getAllClassRooms();
        return ResponseEntity.ok(classRooms);
    }

    @GetMapping("/classes/grade/{gradeName}")
    @PreAuthorize("hasAnyRole('NURSE', 'ADMIN', 'TEACHER', 'PARENT')")
    public ResponseEntity<List<String>> getClassesByGrade(@PathVariable String gradeName) {
        List<String> classes = gradeService.getClassNamesByGradeName(gradeName);
        return ResponseEntity.ok(classes);
    }

    // Admin endpoints for CRUD operations
    @PostMapping("/grades")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Grade> createGrade(@RequestBody Grade grade) {
        Grade savedGrade = gradeService.saveGrade(grade);
        return ResponseEntity.ok(savedGrade);
    }

    @PostMapping("/classes")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ClassRoom> createClassRoom(@RequestBody ClassRoom classRoom) {
        ClassRoom savedClassRoom = gradeService.saveClassRoom(classRoom);
        return ResponseEntity.ok(savedClassRoom);
    }

    @DeleteMapping("/grades/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteGrade(@PathVariable Long id) {
        gradeService.deleteGrade(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/classes/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteClassRoom(@PathVariable Long id) {
        gradeService.deleteClassRoom(id);
        return ResponseEntity.noContent().build();
    }
}
