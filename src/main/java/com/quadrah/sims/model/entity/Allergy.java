package com.quadrah.sims.model.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "allergies")
public class Allergy {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @Column(name = "allergy_type", nullable = false)
    private String allergyType;

    @Column(name = "severity")
    private String severity;

    @Column(name = "reaction")
    private String reaction;

    @Column(name = "notes")
    private String notes;

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Student getStudent() { return student; }
    public void setStudent(Student student) { this.student = student; }
    public String getAllergyType() { return allergyType; }
    public void setAllergyType(String allergyType) { this.allergyType = allergyType; }
    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }
    public String getReaction() { return reaction; }
    public void setReaction(String reaction) { this.reaction = reaction; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
