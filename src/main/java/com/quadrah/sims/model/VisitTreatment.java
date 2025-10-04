package com.quadrah.sims.model;

import jakarta.persistence.*;

@Entity
@Table(name = "visit_treatment")
public class VisitTreatment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_visit_id")
    private StudentVisit studentVisit; // This field was missing

    // Constructors, getters, and setters
    public StudentVisit getStudentVisit() {
        return studentVisit;
    }

    public void setStudentVisit(StudentVisit studentVisit) {
        this.studentVisit = studentVisit;
    }
}
