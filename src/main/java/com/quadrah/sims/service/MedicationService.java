package com.quadrah.sims.service;

import com.quadrah.sims.dto.MedicationDTO;
import com.quadrah.sims.exception.ResourceNotFoundException;
import com.quadrah.sims.model.MedicationAdministration;
import com.quadrah.sims.model.MedicationInventory;
import com.quadrah.sims.model.StudentVisit;
import com.quadrah.sims.repository.MedicationAdministrationRepository;
import com.quadrah.sims.repository.MedicationInventoryRepository;
import com.quadrah.sims.repository.StudentVisitRepository;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.criteria.Predicate;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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

    // New method: Get medications with filters and pagination
    public Page<MedicationDTO> getMedicationsWithFilters(
            Pageable pageable,
            String search,
            String status,
            String category,
            LocalDate expiryDateFrom,
            LocalDate expiryDateTo,
            Boolean isActive) {

        Specification<MedicationInventory> spec = Specification.where(null);

        // Search filter (search in medicationName, genericName, supplier)
        if (search != null && !search.isEmpty()) {
            spec = spec.and((root, query, cb) ->
                    cb.or(
                            cb.like(cb.lower(root.get("medicationName")), "%" + search.toLowerCase() + "%"),
                            cb.like(cb.lower(root.get("genericName")), "%" + search.toLowerCase() + "%"),
                            cb.like(cb.lower(root.get("supplier")), "%" + search.toLowerCase() + "%")
                    )
            );
        }

        // Category filter
        if (category != null && !category.isEmpty()) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("category"), category)
            );
        }

        // Status filter (needs custom logic since status is calculated)
        if (status != null && !status.isEmpty()) {
            spec = spec.and((root, query, cb) -> {
                LocalDate today = LocalDate.now();

                if (status.equalsIgnoreCase("Inactive")) {
                    return cb.equal(root.get("isActive"), false);
                } else if (status.equalsIgnoreCase("Out of Stock")) {
                    return cb.equal(root.get("currentStock"), 0);
                } else if (status.equalsIgnoreCase("Low Stock")) {
                    return cb.and(
                            cb.greaterThan(root.get("currentStock"), 0),
                            cb.lessThan(root.get("currentStock"), root.get("minimumStock"))
                    );
                } else if (status.equalsIgnoreCase("Expiring Soon")) {
                    LocalDate threshold = today.plusDays(30);
                    return cb.and(
                            cb.greaterThanOrEqualTo(root.get("expiryDate"), today),
                            cb.lessThanOrEqualTo(root.get("expiryDate"), threshold)
                    );
                } else if (status.equalsIgnoreCase("Expired")) {
                    return cb.lessThan(root.get("expiryDate"), today);
                } else if (status.equalsIgnoreCase("In Stock")) {
                    return cb.and(
                            cb.equal(root.get("isActive"), true),
                            cb.greaterThanOrEqualTo(root.get("currentStock"), root.get("minimumStock")),
                            cb.greaterThan(root.get("expiryDate"), today.plusDays(30))
                    );
                }
                return null;
            });
        }

        // Expiry date range filter
        if (expiryDateFrom != null) {
            spec = spec.and((root, query, cb) ->
                    cb.greaterThanOrEqualTo(root.get("expiryDate"), expiryDateFrom)
            );
        }

        if (expiryDateTo != null) {
            spec = spec.and((root, query, cb) ->
                    cb.lessThanOrEqualTo(root.get("expiryDate"), expiryDateTo)
            );
        }

        // Active status filter
        if (isActive != null) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("isActive"), isActive)
            );
        }

        // Apply pagination and sorting
        Page<MedicationInventory> medicationsPage = inventoryRepository.findAll(spec, pageable);

        // Convert to DTOs
        List<MedicationDTO> medicationDTOs = medicationsPage.getContent().stream()
                .map(medication -> {
                    MedicationDTO dto = new MedicationDTO();
                    dto.setId(medication.getId());
                    dto.setMedicationName(medication.getMedicationName());
                    dto.setGenericName(medication.getGenericName());
                    dto.setBrand(medication.getSupplier());
                    dto.setDosageForm(medication.getDosageForm());
                    dto.setStrength(medication.getStrength());
                    dto.setCurrentStock(medication.getCurrentStock());
                    dto.setMinimumStock(medication.getMinimumStock());
                    dto.setExpiryDate(medication.getExpiryDate());
                    dto.setSupplier(medication.getSupplier());
                    dto.setIsActive(medication.getIsActive());

                    // Calculate status and days until expiry
                    dto.setDaysUntilExpiry(calculateDaysUntilExpiry(medication.getExpiryDate()));
                    dto.setStatus(calculateStatus(medication, dto.getDaysUntilExpiry()));

                    return dto;
                })
                .collect(Collectors.toList());

        return new PageImpl<>(medicationDTOs, pageable, medicationsPage.getTotalElements());
    }

    private Long calculateDaysUntilExpiry(LocalDate expiryDate) {
        if (expiryDate == null) return null;
        LocalDate today = LocalDate.now();
        return ChronoUnit.DAYS.between(today, expiryDate);
    }

    private String calculateStatus(MedicationInventory medication, Long daysUntilExpiry) {
        if (!Boolean.TRUE.equals(medication.getIsActive())) {
            return "Inactive";
        }

        if (medication.getCurrentStock() == 0) {
            return "Out of Stock";
        }

        if (medication.getCurrentStock() < medication.getMinimumStock()) {
            return "Low Stock";
        }

        if (daysUntilExpiry != null && daysUntilExpiry <= 30 && daysUntilExpiry > 0) {
            return "Expiring Soon";
        }

        if (daysUntilExpiry != null && daysUntilExpiry <= 0) {
            return "Expired";
        }

        return "In Stock";
    }

    // Update existing methods to use DTOs where needed
    public List<MedicationInventory> getAllMedications() {
        return inventoryRepository.findByIsActiveTrue();
    }

    public List<MedicationDTO> getAllMedicationsDTO() {
        return inventoryRepository.findAll().stream()
                .map(medication -> {
                    MedicationDTO dto = new MedicationDTO();
                    dto.setId(medication.getId());
                    dto.setMedicationName(medication.getMedicationName());
                    dto.setGenericName(medication.getGenericName());
                    dto.setBrand(medication.getSupplier());
                    dto.setDosageForm(medication.getDosageForm());
                    dto.setStrength(medication.getStrength());
                    dto.setCurrentStock(medication.getCurrentStock());
                    dto.setMinimumStock(medication.getMinimumStock());
                    dto.setExpiryDate(medication.getExpiryDate());
                    dto.setSupplier(medication.getSupplier());
                    dto.setIsActive(medication.getIsActive());

                    Long daysUntilExpiry = calculateDaysUntilExpiry(medication.getExpiryDate());
                    dto.setDaysUntilExpiry(daysUntilExpiry);
                    dto.setStatus(calculateStatus(medication, daysUntilExpiry));

                    return dto;
                })
                .collect(Collectors.toList());
    }

    public MedicationInventory getMedicationById(Long id) {
        return inventoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Medication not found with id: " + id));
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
                .orElseThrow(() -> new ResourceNotFoundException("Medication not found with id: " + id));

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
                .orElseThrow(() -> new ResourceNotFoundException("Medication not found with id: " + medicationId));

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
                .orElseThrow(() -> new ResourceNotFoundException("Medication not found with id: " + id));

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
                .orElseThrow(() -> new ResourceNotFoundException("Visit not found with id: " + visitId));
        return administrationRepository.findByStudentVisitOrderByAdministrationTimeDesc(visit);
    }

    public List<MedicationAdministration> getMedicationAdministrationsByStudent(Long studentId) {
        return administrationRepository.findByStudentId(studentId);
    }

    public List<MedicationAdministration> getRecentMedicationAdministrations() {
        return administrationRepository.findTop10ByOrderByAdministrationTimeDesc();
    }

//    public List<String> getAllCategories() {
//        return inventoryRepository.findAllCategories();
//    }

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
        if (medication.getExpiryDate() == null) {
            throw new IllegalArgumentException("Expiry date is required.");
        }
        if (medication.getExpiryDate().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Expiry date cannot be in the past.");
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