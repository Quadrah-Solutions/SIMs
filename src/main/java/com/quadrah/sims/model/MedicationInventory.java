package com.quadrah.sims.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "medication_inventory")
public class MedicationInventory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "medication_name", nullable = false)
    private String medicationName;

    @Column(name = "generic_name")
    private String genericName;

    @Column(name = "current_stock")
    private Integer currentStock = 0;

    @Column(name = "minimum_stock")
    private Integer minimumStock = 5;

    @Column(name = "dosage_form")
    private String dosageForm; // Tablets, Liquid, etc.

    @Column(name = "strength")
    private String strength;

    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    @Column(name = "supplier")
    private String supplier;

    @Column(name = "is_active")
    private Boolean isActive = true;

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getMedicationName() { return medicationName; }
    public void setMedicationName(String medicationName) { this.medicationName = medicationName; }
    public String getGenericName() { return genericName; }
    public void setGenericName(String genericName) { this.genericName = genericName; }
    public Integer getCurrentStock() { return currentStock; }
    public void setCurrentStock(Integer currentStock) { this.currentStock = currentStock; }
    public Integer getMinimumStock() { return minimumStock; }
    public void setMinimumStock(Integer minimumStock) { this.minimumStock = minimumStock; }
    public String getDosageForm() { return dosageForm; }
    public void setDosageForm(String dosageForm) { this.dosageForm = dosageForm; }
    public String getStrength() { return strength; }
    public void setStrength(String strength) { this.strength = strength; }
    public LocalDate getExpiryDate() { return expiryDate; }
    public void setExpiryDate(LocalDate expiryDate) { this.expiryDate = expiryDate; }
    public String getSupplier() { return supplier; }
    public void setSupplier(String supplier) { this.supplier = supplier; }
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
}
