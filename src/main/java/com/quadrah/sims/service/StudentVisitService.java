package com.quadrah.sims.service;

import com.quadrah.sims.dto.VisitDTO;
import com.quadrah.sims.exception.InsufficientStockException;
import com.quadrah.sims.exception.ResourceNotFoundException;
import com.quadrah.sims.model.*;
import com.quadrah.sims.repository.MedicationInventoryRepository;
import com.quadrah.sims.repository.StudentRepository;
import com.quadrah.sims.repository.StudentVisitRepository;
import com.quadrah.sims.repository.UserAccountRepository;
import jakarta.persistence.criteria.Join;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@Transactional
public class StudentVisitService {

    private final StudentVisitRepository visitRepository;
    private final StudentRepository studentRepository;
    private final UserAccountRepository userAccountRepository;
    private final NotificationService notificationService;

    private final MedicationInventoryRepository medicationInventoryRepository;

    public StudentVisitService(StudentVisitRepository visitRepository,
                               StudentRepository studentRepository,
                               UserAccountRepository userAccountRepository,
                               NotificationService notificationService,
                               MedicationInventoryRepository medicationInventoryRepository) {
        this.visitRepository = visitRepository;
        this.studentRepository = studentRepository;
        this.userAccountRepository = userAccountRepository;
        this.notificationService = notificationService;
        this.medicationInventoryRepository = medicationInventoryRepository;
    }

    public List<StudentVisit> getAllVisits() {
        return visitRepository.findAll();
    }

    public List<VisitDTO> getAllVisitsDTO() {
        List<StudentVisit> visits = visitRepository.findAll();

        return visits.stream()
                .map(VisitDTO::new)
                .collect(Collectors.toList());
    }

    public StudentVisit getVisitById(Long id) {
        return visitRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Visit not found with id: " + id));
    }

    public List<StudentVisit> getVisitsByStudent(Long studentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found with id: " + studentId));
        return visitRepository.findByStudentOrderByVisitDateDesc(student);
    }

    public List<StudentVisit> getVisitsByNurse(Long nurseId) {
        UserAccount nurse = userAccountRepository.findById(nurseId)
                .orElseThrow(() -> new IllegalArgumentException("Nurse not found with id: " + nurseId));
        return visitRepository.findByNurseOrderByVisitDateDesc(nurse);
    }

    public List<StudentVisit> getVisitsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("Start date and end date are required.");
        }
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Start date cannot be after end date.");
        }
        return visitRepository.findByVisitDateBetweenOrderByVisitDateDesc(startDate, endDate);
    }

    public List<StudentVisit> getEmergencyVisits() {
        return visitRepository.findByEmergencyFlagTrueOrderByVisitDateDesc();
    }

    public List<StudentVisit> getActiveObservations() {
        return visitRepository.findByDispositionIsNullOrderByVisitDateDesc();
    }

    @Transactional
    public StudentVisit createVisit(StudentVisit visit) {
        validateVisit(visit);

        // Set visit date to now if not provided
        if (visit.getVisitDate() == null) {
            visit.setVisitDate(LocalDateTime.now());
        }

        // CRITICAL: Set the studentVisit reference in all medications
        if (visit.getMedications() != null) {
            for (MedicationAdministration medication : visit.getMedications()) {
                medication.setStudentVisit(visit);
                // Also set administration time if not already set
                if (medication.getAdministrationTime() == null) {
                    medication.setAdministrationTime(LocalDateTime.now());
                }

                // NEW: Deduct medication stock from inventory
                deductMedicationStock(medication);
            }
        }

        // CRITICAL: Set the studentVisit reference in all treatments
        if (visit.getTreatments() != null) {
            for (VisitTreatment treatment : visit.getTreatments()) {
                treatment.setStudentVisit(visit); // THIS LINE IS ABSOLUTELY NECESSARY
            }
        }

        StudentVisit savedVisit = visitRepository.save(visit);

        // Handle emergency notifications
        if (Boolean.TRUE.equals(visit.getEmergencyFlag())) {
            notificationService.notifyEmergencyVisit(savedVisit);
        }

        return savedVisit;
    }

    // NEW METHOD: Deduct medication stock from inventory
    private void deductMedicationStock(MedicationAdministration medication) {
        if (medication.getMedication() == null || medication.getMedication().getId() == null) {
            throw new IllegalArgumentException("Medication must be specified with an ID");
        }

        // Get the medication from inventory
        MedicationInventory medInventory = medicationInventoryRepository
                .findById(medication.getMedication().getId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Medication not found with id: " + medication.getMedication().getId()
                ));

        // Parse quantity from dosage string
        int quantity = parseQuantityFromDosage(medication.getDosage());

        // Check if enough stock is available
        if (medInventory.getCurrentStock() < quantity) {
            throw new InsufficientStockException(
                    "Insufficient stock for " + medInventory.getMedicationName() +
                            ". Available: " + medInventory.getCurrentStock() +
                            ", Requested: " + quantity
            );
        }

        // Check if medication is expired
        if (medInventory.getExpiryDate() != null &&
                medInventory.getExpiryDate().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException(
                    "Cannot dispense expired medication: " + medInventory.getMedicationName() +
                            " expired on " + medInventory.getExpiryDate()
            );
        }

        // Check if medication is active
        if (!Boolean.TRUE.equals(medInventory.getIsActive())) {
            throw new IllegalArgumentException(
                    "Cannot dispense inactive medication: " + medInventory.getMedicationName()
            );
        }

        // Deduct stock
        medInventory.setCurrentStock(medInventory.getCurrentStock() - quantity);

        // Save updated inventory
        medicationInventoryRepository.save(medInventory);

        // Log the stock deduction
//        log.info("Deducted {} units of {} (ID: {}). New stock: {}",
//                quantity, medInventory.getMedicationName(),
//                medInventory.getId(), medInventory.getCurrentStock());
    }

    // Helper method to parse quantity from dosage string
    private int parseQuantityFromDosage(String dosage) {
        if (dosage == null || dosage.trim().isEmpty()) {
            return 1; // Default to 1 if no dosage specified
        }

        try {
            // Extract first number from dosage string (e.g., "2 tablets" -> 2)
            Pattern pattern = Pattern.compile("(\\d+)");
            Matcher matcher = pattern.matcher(dosage);
            if (matcher.find()) {
                return Integer.parseInt(matcher.group(1));
            }
        } catch (Exception e) {
//            log.warn("Could not parse quantity from dosage: '{}'. Using default 1.", dosage, e);
        }

        return 1; // Default to 1 if can't parse
    }

    public StudentVisit updateVisit(Long id, StudentVisit visitDetails) {
        StudentVisit visit = visitRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Visit not found with id: " + id));

        validateVisit(visitDetails);

        visit.setReason(visitDetails.getReason());
        visit.setSymptoms(visitDetails.getSymptoms());
        visit.setObservations(visitDetails.getObservations());
        visit.setVitalSigns(visitDetails.getVitalSigns());
        visit.setFinalAssessment(visitDetails.getFinalAssessment());
        visit.setReferredBy(visitDetails.getReferredBy());

        // Handle disposition change
        if (visitDetails.getDisposition() != null &&
                !visitDetails.getDisposition().equals(visit.getDisposition())) {
            visit.setDisposition(visitDetails.getDisposition());
            notificationService.notifyDispositionChange(visit);
        }

        // Handle emergency flag change
        if (Boolean.TRUE.equals(visitDetails.getEmergencyFlag()) &&
                !Boolean.TRUE.equals(visit.getEmergencyFlag())) {
            visit.setEmergencyFlag(true);
            notificationService.notifyEmergencyVisit(visit);
        }

        return visitRepository.save(visit);
    }

    public StudentVisit updateDisposition(Long visitId, StudentVisit.DispositionType disposition) {
        StudentVisit visit = visitRepository.findById(visitId)
                .orElseThrow(() -> new IllegalArgumentException("Visit not found with id: " + visitId));

        visit.setDisposition(disposition);

        StudentVisit updatedVisit = visitRepository.save(visit);
        notificationService.notifyDispositionChange(updatedVisit);

        return updatedVisit;
    }

    public void deleteVisit(Long id) {
        StudentVisit visit = visitRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Visit not found with id: " + id));

        // Check if visit has associated medications (optional business rule)
        if (!visit.getMedications().isEmpty()) {
            throw new IllegalStateException("Cannot delete visit with associated medication records.");
        }

        visitRepository.delete(visit);
    }

    public long getVisitCountByStudent(Long studentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found with id: " + studentId));
        return visitRepository.countByStudent(student);
    }

    public List<StudentVisit> getRecentVisits(int days) {
        LocalDateTime sinceDate = LocalDateTime.now().minusDays(days);
        return visitRepository.findRecentVisits(sinceDate);
    }

    private void validateVisit(StudentVisit visit) {
        if (visit.getStudent() == null || visit.getStudent().getId() == null) {
            throw new IllegalArgumentException("Student is required.");
        }
        if (visit.getNurse() == null || visit.getNurse().getId() == null) {
            throw new IllegalArgumentException("Nurse is required.");
        }
        if (visit.getReason() == null || visit.getReason().trim().isEmpty()) {
            throw new IllegalArgumentException("Visit reason is required.");
        }

        // Verify student exists
        if (!studentRepository.existsById(visit.getStudent().getId())) {
            throw new IllegalArgumentException("Student not found with id: " + visit.getStudent().getId());
        }

        // Verify nurse exists and is actually a nurse
        UserAccount nurse = userAccountRepository.findById(visit.getNurse().getId())
                .orElseThrow(() -> new IllegalArgumentException("Nurse not found with id: " + visit.getNurse().getId()));

        if (nurse.getRole() != UserAccount.UserRole.NURSE && nurse.getRole() != UserAccount.UserRole.ADMIN) {
            throw new IllegalArgumentException("User is not authorized to create visits.");
        }
    }

    public Page<VisitDTO> getVisitsWithFilters(
            Pageable pageable,
            String search,
            String studentName,
            String grade,
            String className,
            String condition,
            LocalDate dateFrom,
            LocalDate dateTo,
            Boolean emergencyFlag) {

        Specification<StudentVisit> spec = Specification.where(null);

        // Search filter (searches in student name and reason)
        if (search != null && !search.isEmpty()) {
            spec = spec.and((root, query, cb) ->
                    cb.or(
                            cb.like(cb.lower(root.get("student").get("firstName")), "%" + search.toLowerCase() + "%"),
                            cb.like(cb.lower(root.get("student").get("lastName")), "%" + search.toLowerCase() + "%"),
                            cb.like(cb.lower(root.get("reason")), "%" + search.toLowerCase() + "%")
                    )
            );
        }

        // Student name filter (exact name match)
        if (studentName != null && !studentName.isEmpty()) {
            spec = spec.and((root, query, cb) ->
                    cb.like(cb.lower(
                            cb.concat(root.get("student").get("firstName"),
                                    cb.concat(" ", root.get("student").get("lastName")))
                    ), "%" + studentName.toLowerCase() + "%")
            );
        }

        // Grade filter (from student's grade entity)
        if (grade != null && !grade.isEmpty()) {
            spec = spec.and((root, query, cb) -> {
                // Join to grade entity and check name
                Join<Student, Grade> gradeJoin = root.join("student").join("grade");
                return cb.equal(cb.lower(gradeJoin.get("gradeName")), grade.toLowerCase());
            });
        }

        // Class filter (from student's classRoom entity)
        if (className != null && !className.isEmpty()) {
            spec = spec.and((root, query, cb) -> {
                // Join to classRoom entity and check name
                Join<Student, ClassRoom> classJoin = root.join("student").join("classRoom");
                return cb.equal(cb.lower(classJoin.get("name")), className.toLowerCase());
            });
        }

        // Condition filter (determined from emergencyFlag and disposition)
        if (condition != null && !condition.isEmpty()) {
            if (condition.equalsIgnoreCase("Critical")) {
                spec = spec.and((root, query, cb) ->
                        cb.equal(root.get("emergencyFlag"), true)
                );
            } else if (condition.equalsIgnoreCase("Serious")) {
                spec = spec.and((root, query, cb) ->
                        cb.equal(root.get("disposition"), StudentVisit.DispositionType.REFERRED_TO_HOSPITAL)
                );
            } else if (condition.equalsIgnoreCase("Moderate")) {
                spec = spec.and((root, query, cb) ->
                        cb.equal(root.get("disposition"), StudentVisit.DispositionType.SENT_HOME)
                );
            } else if (condition.equalsIgnoreCase("Stable") || condition.equalsIgnoreCase("Mild")) {
                spec = spec.and((root, query, cb) ->
                        cb.or(
                                cb.equal(root.get("disposition"), StudentVisit.DispositionType.UNDER_OBSERVATION),
                                cb.equal(root.get("disposition"), StudentVisit.DispositionType.RETURNED_TO_CLASS)
                        )
                );
            }
        }

        // Date range filters
        if (dateFrom != null) {
            spec = spec.and((root, query, cb) ->
                    cb.greaterThanOrEqualTo(root.get("visitDate"), dateFrom.atStartOfDay())
            );
        }

        if (dateTo != null) {
            spec = spec.and((root, query, cb) ->
                    cb.lessThanOrEqualTo(root.get("visitDate"), dateTo.atTime(23, 59, 59))
            );
        }

        // Emergency flag filter
        if (emergencyFlag != null) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("emergencyFlag"), emergencyFlag)
            );
        }

        // Apply pagination and sorting
        Page<StudentVisit> visitsPage = visitRepository.findAll(spec, pageable);

        // Convert to VisitDTO with grade and class information
        List<VisitDTO> visitDTOs = visitsPage.getContent().stream()
                .map(visit -> {
                    VisitDTO dto = new VisitDTO(visit);
                    // Add grade and class information from student's entities
                    if (visit.getStudent() != null) {
                        // Use gradeLevel from student (direct string field)
                        dto.setGrade(visit.getStudent().getGradeLevel());

                        // Get class name from classRoom entity if exists
                        if (visit.getStudent().getClassRoom() != null) {
                            dto.setClassName(visit.getStudent().getClassRoom().getClassName());
                        } else {
                            dto.setClassName(visit.getStudent().getHomeroom());
                        }
                    }
                    return dto;
                })
                .collect(Collectors.toList());

        return new PageImpl<>(visitDTOs, pageable, visitsPage.getTotalElements());
    }
}
