package com.quadrah.sims.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "medication_administrations")
public class MedicationAdministration {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "visit_id", nullable = false)
    private StudentVisit studentVisit;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medication_id")
    private MedicationInventory medication;

    @Column(name = "medication_name", nullable = false)
    private String medicationName;

    @Column(name = "dosage")
    private String dosage;

    @Column(name = "administration_time", nullable = false)
    private LocalDateTime administrationTime;

    @Column(name = "batch_number")
    private String batchNumber;

    @Column(name = "notes", length = 1000)
    private String notes;

    @Column(name = "administered_by", nullable = false)
    private String administeredBy; // Nurse name

    // Constructors
    public MedicationAdministration() {
        this.administrationTime = LocalDateTime.now();
    }

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public StudentVisit getStudentVisit() { return studentVisit; }
    public void setStudentVisit(StudentVisit studentVisit) { this.studentVisit = studentVisit; }
    public MedicationInventory getMedication() { return medication; }
    public void setMedication(MedicationInventory medication) { this.medication = medication; }
    public String getMedicationName() { return medicationName; }
    public void setMedicationName(String medicationName) { this.medicationName = medicationName; }
    public String getDosage() { return dosage; }
    public void setDosage(String dosage) { this.dosage = dosage; }
    public LocalDateTime getAdministrationTime() { return administrationTime; }
    public void setAdministrationTime(LocalDateTime administrationTime) { this.administrationTime = administrationTime; }
    public String getBatchNumber() { return batchNumber; }
    public void setBatchNumber(String batchNumber) { this.batchNumber = batchNumber; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public String getAdministeredBy() { return administeredBy; }
    public void setAdministeredBy(String administeredBy) { this.administeredBy = administeredBy; }
}
