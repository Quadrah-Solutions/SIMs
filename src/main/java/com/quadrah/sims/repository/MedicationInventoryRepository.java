package com.quadrah.sims.repository;

import com.quadrah.sims.model.MedicationInventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface MedicationInventoryRepository extends JpaRepository<MedicationInventory, Long> {

    // Find medication by name
    Optional<MedicationInventory> findByMedicationName(String medicationName);

    // Find medications with low stock (current stock <= minimum stock)
    @Query("SELECT m FROM MedicationInventory m WHERE m.currentStock <= m.minimumStock AND m.isActive = true")
    List<MedicationInventory> findLowStockMedications();

    // Find medications that are expired or expiring soon
    @Query("SELECT m FROM MedicationInventory m WHERE m.expiryDate <= :expiryThreshold AND m.isActive = true ORDER BY m.expiryDate ASC")
    List<MedicationInventory> findExpiringMedications(@Param("expiryThreshold") LocalDate expiryThreshold);

    // Find active medications
    List<MedicationInventory> findByIsActiveTrue();

    // Search medications by name
    List<MedicationInventory> findByMedicationNameContainingIgnoreCase(String name);

    // Find medications that need reordering (custom threshold)
    @Query("SELECT m FROM MedicationInventory m WHERE m.currentStock <= :threshold AND m.isActive = true")
    List<MedicationInventory> findMedicationsNeedingReorder(@Param("threshold") Integer threshold);

    // Check if medication name exists (for validation)
    boolean existsByMedicationName(String medicationName);
}
