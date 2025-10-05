package com.quadrah.sims.model;

import jakarta.persistence.*;

@Entity
@Table(name = "medical_histories")
public class MedicalHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @Column(name = "condition_name", nullable = false)
    private String conditionName;

    @Column(name = "diagnosis_date")
    private java.time.LocalDate diagnosisDate;

    @Column(name = "severity")
    private String severity;

    @Column(name = "treatment")
    private String treatment;

    @Column(name = "notes", length = 1000)
    private String notes;

    @Column(name = "is_active")
    private Boolean isActive = true;

    // Constructors
    public MedicalHistory() {}

    public MedicalHistory(Student student, String conditionName) {
        this.student = student;
        this.conditionName = conditionName;
    }

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Student getStudent() { return student; }
    public void setStudent(Student student) { this.student = student; }

    public String getConditionName() { return conditionName; }
    public void setConditionName(String conditionName) { this.conditionName = conditionName; }

    public java.time.LocalDate getDiagnosisDate() { return diagnosisDate; }
    public void setDiagnosisDate(java.time.LocalDate diagnosisDate) { this.diagnosisDate = diagnosisDate; }

    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }

    public String getTreatment() { return treatment; }
    public void setTreatment(String treatment) { this.treatment = treatment; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
}