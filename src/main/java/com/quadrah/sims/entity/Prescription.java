package com.quadrah.sims.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;

@Data
@Entity
@Table(name = "prescriptions")
public class Prescription extends BaseEntity{
    private String patientName;
    private String medication;
    private String dosage;
    private int quantity;
    private LocalDate datePrescribed;
    private LocalDate dateFilled;
    private Integer refillsRemaining;
    private Integer totalRefillsAllowed;
    private LocalDate lastFilledDate;
    private Integer daysSupply;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id")
    private Patient patient;

    public Prescription() {}

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PrescriptionStatus status = PrescriptionStatus.PENDING;

    public enum PrescriptionStatus {
        PENDING,
        FILLED,
        DISPENSED,
        ON_HOLD,
        CANCELLED,
        EXPIRED
    }
}
