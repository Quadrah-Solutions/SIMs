package com.quadrah.sims.model.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "student_visits")
public class StudentVisit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nurse_id", nullable = false)
    private UserAccount nurse;

    @Column(name = "visit_date", nullable = false)
    private LocalDateTime visitDate;

    @Column(name = "reason", length = 500)
    private String reason;

    @Column(name = "symptoms", length = 1000)
    private String symptoms;

    @Column(name = "observations", length = 2000)
    private String observations;

    @Column(name = "vital_signs", length = 500)
    private String vitalSigns; // JSON string or formatted text

    @Enumerated(EnumType.STRING)
    @Column(name = "disposition")
    private DispositionType disposition;

    @Column(name = "disposition_time")
    private LocalDateTime dispositionTime;

    @Column(name = "final_assessment", length = 2000)
    private String finalAssessment;

    @Column(name = "emergency_flag")
    private Boolean emergencyFlag = false;

    @Column(name = "referred_by")
    private String referredBy; // Teacher name or "Self"

    @OneToMany(mappedBy = "studentVisit", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<MedicationAdministration> medications = new ArrayList<>();

    @OneToMany(mappedBy = "studentVisit", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<VisitTreatment> treatments = new ArrayList<>();

    public enum DispositionType {
        RETURNED_TO_CLASS, SENT_HOME, UNDER_OBSERVATION, REFERRED_TO_HOSPITAL
    }

    // Constructors
    public StudentVisit() {
        this.visitDate = LocalDateTime.now();
    }

    // Pre-persist method to set disposition time when disposition is set
    @PreUpdate
    @PrePersist
    public void setDispositionTimestamp() {
        if (this.disposition != null && this.dispositionTime == null) {
            this.dispositionTime = LocalDateTime.now();
        }
    }

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Student getStudent() { return student; }
    public void setStudent(Student student) { this.student = student; }
    public UserAccount getNurse() { return nurse; }
    public void setNurse(UserAccount nurse) { this.nurse = nurse; }
    public LocalDateTime getVisitDate() { return visitDate; }
    public void setVisitDate(LocalDateTime visitDate) { this.visitDate = visitDate; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public String getSymptoms() { return symptoms; }
    public void setSymptoms(String symptoms) { this.symptoms = symptoms; }
    public String getObservations() { return observations; }
    public void setObservations(String observations) { this.observations = observations; }
    public String getVitalSigns() { return vitalSigns; }
    public void setVitalSigns(String vitalSigns) { this.vitalSigns = vitalSigns; }
    public DispositionType getDisposition() { return disposition; }
    public void setDisposition(DispositionType disposition) { this.disposition = disposition; }
    public LocalDateTime getDispositionTime() { return dispositionTime; }
    public void setDispositionTime(LocalDateTime dispositionTime) { this.dispositionTime = dispositionTime; }
    public String getFinalAssessment() { return finalAssessment; }
    public void setFinalAssessment(String finalAssessment) { this.finalAssessment = finalAssessment; }
    public Boolean getEmergencyFlag() { return emergencyFlag; }
    public void setEmergencyFlag(Boolean emergencyFlag) { this.emergencyFlag = emergencyFlag; }
    public String getReferredBy() { return referredBy; }
    public void setReferredBy(String referredBy) { this.referredBy = referredBy; }
    public List<MedicationAdministration> getMedications() { return medications; }
    public void setMedications(List<MedicationAdministration> medications) { this.medications = medications; }
    public List<VisitTreatment> getTreatments() { return treatments; }
    public void setTreatments(List<VisitTreatment> treatments) { this.treatments = treatments; }
}
