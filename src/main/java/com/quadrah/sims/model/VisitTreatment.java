package com.quadrah.sims.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;

@Entity
@Table(name = "visit_treatment")
public class VisitTreatment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_visit_id")
    @JsonBackReference("visit-treatments")  // ADD THIS
    private StudentVisit studentVisit;

    @Column(name = "treatment_name")
    private String treatmentName;

    @Column(name = "description")
    private String description;

    @Column(name = "duration")
    private String duration;

    @Column(name = "notes")
    private String notes;

    @Column(name = "administered_by")
    private String administeredBy;

    // Constructors
    public VisitTreatment() {}

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public StudentVisit getStudentVisit() { return studentVisit; }
    public void setStudentVisit(StudentVisit studentVisit) { this.studentVisit = studentVisit; }
    public String getTreatmentName() { return treatmentName; }
    public void setTreatmentName(String treatmentName) { this.treatmentName = treatmentName; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getDuration() { return duration; }
    public void setDuration(String duration) { this.duration = duration; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public String getAdministeredBy() { return administeredBy; }
    public void setAdministeredBy(String administeredBy) { this.administeredBy = administeredBy; }
}