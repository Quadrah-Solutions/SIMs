package com.quadrah.sims.service;

import com.quadrah.sims.model.MedicationAdministration;
import com.quadrah.sims.model.MedicationInventory;
import com.quadrah.sims.model.StudentVisit;
import com.quadrah.sims.repository.MedicationAdministrationRepository;
import com.quadrah.sims.repository.MedicationInventoryRepository;
import com.quadrah.sims.repository.StudentVisitRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class MedicationService {

    private final MedicationInventoryRepository inventoryRepository;
    private final MedicationAdministrationRepository administrationRepository;
    private final StudentVisitRepository visitRepository;
    private final NotificationService notificationService;

    public MedicationService(MedicationInventoryRepository inventoryRepository,
                             MedicationAdministrationRepository administrationRepository,
                             StudentVisitRepository visitRepository,
                             NotificationService notificationService) {
        this.inventoryRepository = inventoryRepository;
        this.administrationRepository = administrationRepository;
        this.visitRepository = visitRepository;
        this.notificationService = notificationService;
    }

    // Medication Inventory Methods
    public List<MedicationInventory> getAllMedications() {
        return inventoryRepository.findByIsActiveTrue();
    }

    public MedicationInventory getMedicationById(Long id) {
        return inventoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Medication not found with id: " + id));
    }

    public MedicationInventory createMedication(MedicationInventory medication) {
        validateMedication(medication);

        if (inventoryRepository.existsByMedicationName(medication.getMedicationName())) {
            throw new IllegalArgumentException("Medication with name '" + medication.getMedicationName() + "' already exists.");
        }

        medication.setIsActive(true);
        return inventoryRepository.save(medication);
    }

    public MedicationInventory updateMedication(Long id, MedicationInventory medicationDetails) {
        MedicationInventory medication = inventoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Medication not found with id: " + id));

        validateMedication(medicationDetails);

        // Check if name is being changed to an existing one
        if (!medication.getMedicationName().equals(medicationDetails.getMedicationName()) &&
                inventoryRepository.existsByMedicationName(medicationDetails.getMedicationName())) {
            throw new IllegalArgumentException("Medication with name '" + medicationDetails.getMedicationName() + "' already exists.");
        }

        medication.setMedicationName(medicationDetails.getMedicationName());
        medication.setGenericName(medicationDetails.getGenericName());
        medication.setDosageForm(medicationDetails.getDosageForm());
        medication.setStrength(medicationDetails.getStrength());
        medication.setMinimumStock(medicationDetails.getMinimumStock());
        medication.setSupplier(medicationDetails.getSupplier());
        medication.setExpiryDate(medicationDetails.getExpiryDate());

        return inventoryRepository.save(medication);
    }

    public MedicationInventory updateStock(Long medicationId, Integer quantityChange, String reason) {
        MedicationInventory medication = inventoryRepository.findById(medicationId)
                .orElseThrow(() -> new IllegalArgumentException("Medication not found with id: " + medicationId));

        int newStock = medication.getCurrentStock() + quantityChange;
        if (newStock < 0) {
            throw new IllegalArgumentException("Insufficient stock. Current stock: " + medication.getCurrentStock());
        }

        medication.setCurrentStock(newStock);

        // Check for low stock alert
        if (newStock <= medication.getMinimumStock()) {
            notificationService.notifyLowStock(medication);
        }

        return inventoryRepository.save(medication);
    }

    public void deactivateMedication(Long id) {
        MedicationInventory medication = inventoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Medication not found with id: " + id));

        medication.setIsActive(false);
        inventoryRepository.save(medication);
    }

    public List<MedicationInventory> getLowStockMedications() {
        return inventoryRepository.findLowStockMedications();
    }

    public List<MedicationInventory> getExpiringMedications(int daysThreshold) {
        LocalDate thresholdDate = LocalDate.now().plusDays(daysThreshold);
        return inventoryRepository.findExpiringMedications(thresholdDate);
    }

    // Medication Administration Methods
    public MedicationAdministration administerMedication(MedicationAdministration administration) {
        validateMedicationAdministration(administration);

        // Check stock availability
        if (administration.getMedication() != null) {
            MedicationInventory medication = administration.getMedication();
            if (medication.getCurrentStock() <= 0) {
                throw new IllegalStateException("Medication '" + medication.getMedicationName() + "' is out of stock.");
            }

            // Deduct from inventory
            medication.setCurrentStock(medication.getCurrentStock() - 1);
            inventoryRepository.save(medication);
        }

        administration.setAdministrationTime(LocalDateTime.now());
        return administrationRepository.save(administration);
    }

    public List<MedicationAdministration> getMedicationAdministrationsByVisit(Long visitId) {
        StudentVisit visit = visitRepository.findById(visitId)
                .orElseThrow(() -> new IllegalArgumentException("Visit not found with id: " + visitId));
        return administrationRepository.findByStudentVisitOrderByAdministrationTimeDesc(visit);
    }

    public List<MedicationAdministration> getMedicationAdministrationsByStudent(Long studentId) {
        return administrationRepository.findByStudentId(studentId);
    }

    public List<MedicationAdministration> getRecentMedicationAdministrations() {
        return administrationRepository.findTop10ByOrderByAdministrationTimeDesc();
    }

    private void validateMedication(MedicationInventory medication) {
        if (medication.getMedicationName() == null || medication.getMedicationName().trim().isEmpty()) {
            throw new IllegalArgumentException("Medication name is required.");
        }
        if (medication.getCurrentStock() == null || medication.getCurrentStock() < 0) {
            throw new IllegalArgumentException("Current stock cannot be negative.");
        }
        if (medication.getMinimumStock() == null || medication.getMinimumStock() < 0) {
            throw new IllegalArgumentException("Minimum stock cannot be negative.");
        }
    }

    private void validateMedicationAdministration(MedicationAdministration administration) {
        if (administration.getStudentVisit() == null || administration.getStudentVisit().getId() == null) {
            throw new IllegalArgumentException("Student visit is required.");
        }
        if (administration.getMedicationName() == null || administration.getMedicationName().trim().isEmpty()) {
            throw new IllegalArgumentException("Medication name is required.");
        }
        if (administration.getAdministeredBy() == null || administration.getAdministeredBy().trim().isEmpty()) {
            throw new IllegalArgumentException("Administering nurse name is required.");
        }

        // Verify visit exists
        if (!visitRepository.existsById(administration.getStudentVisit().getId())) {
            throw new IllegalArgumentException("Visit not found with id: " + administration.getStudentVisit().getId());
        }
    }
}
