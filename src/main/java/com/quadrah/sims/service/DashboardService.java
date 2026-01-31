package com.quadrah.sims.service;

import com.quadrah.sims.model.StudentVisit;
import com.quadrah.sims.model.MedicationInventory;
import com.quadrah.sims.model.Student;
import com.quadrah.sims.repository.StudentVisitRepository;
import com.quadrah.sims.repository.MedicationInventoryRepository;
import com.quadrah.sims.repository.StudentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class DashboardService {

    private final StudentVisitRepository visitRepository;
    private final MedicationInventoryRepository medicationRepository;
    private final StudentRepository studentRepository;

    private static final Logger log = LoggerFactory.getLogger(DashboardService.class);

    public DashboardService(StudentVisitRepository visitRepository,
                            MedicationInventoryRepository medicationRepository,
                            StudentRepository studentRepository) {
        this.visitRepository = visitRepository;
        this.medicationRepository = medicationRepository;
        this.studentRepository = studentRepository;
    }

    public Map<String, Object> getDashboardStats() {
        try {
            log.info("Fetching dashboard statistics...");

            LocalDate today = LocalDate.now();
            LocalDateTime startOfDay = today.atStartOfDay();
            LocalDateTime endOfDay = today.atTime(23, 59, 59);

            // Get today's visits
            List<StudentVisit> todaysVisits = visitRepository.findByVisitDateBetween(startOfDay, endOfDay);
            log.info("Found {} visits for today", todaysVisits.size());

            // Get all medications
            List<MedicationInventory> medications = medicationRepository.findAll();
            log.info("Found {} medications", medications.size());

            // Get student count
            long totalStudents = studentRepository.count();
            log.info("Total students: {}", totalStudents);

            // Calculate statistics with null safety
            long todaysVisitsCount = todaysVisits.size();

            long medicationGiven = todaysVisits.stream()
                    .filter(visit -> visit.getMedications() != null)
                    .flatMap(visit -> visit.getMedications().stream())
                    .count();

            long lowStockAlerts = medications.stream()
                    .filter(med -> med.getCurrentStock() != null &&
                            med.getMinimumStock() != null &&
                            med.getCurrentStock() < med.getMinimumStock() &&
                            med.getCurrentStock() > 0)
                    .count();

            long studentsCleared = todaysVisits.stream()
                    .filter(visit -> visit.getDisposition() != null &&
                            "RETURNED_TO_CLASS".equals(visit.getDisposition().name()))
                    .count();

            long criticalCases = todaysVisits.stream()
                    .filter(visit -> Boolean.TRUE.equals(visit.getEmergencyFlag()))
                    .count();

            // Create response
            Map<String, Object> stats = new HashMap<>();
            stats.put("todaysVisits", todaysVisitsCount);
            stats.put("medicationGiven", medicationGiven);
            stats.put("lowStockAlerts", lowStockAlerts);
            stats.put("studentsCleared", studentsCleared);
            stats.put("totalStudents", totalStudents);
            stats.put("criticalCases", criticalCases);
            stats.put("timestamp", LocalDateTime.now());

            log.info("Dashboard stats calculated successfully");
            return stats;

        } catch (Exception e) {
            log.error("Error calculating dashboard statistics", e);
            // Return empty stats instead of throwing
            Map<String, Object> errorStats = new HashMap<>();
            errorStats.put("todaysVisits", 0);
            errorStats.put("medicationGiven", 0);
            errorStats.put("lowStockAlerts", 0);
            errorStats.put("studentsCleared", 0);
            errorStats.put("totalStudents", 0);
            errorStats.put("criticalCases", 0);
            errorStats.put("timestamp", LocalDateTime.now());
            return errorStats;
        }
    }

    public Map<String, Object> getAnalytics(LocalDate startDate, LocalDate endDate) {
        try {
            log.info("Fetching analytics from {} to {}", startDate, endDate);

            LocalDateTime startDateTime = startDate.atStartOfDay();
            LocalDateTime endDateTime = endDate.atTime(23, 59, 59);

            // Get visits in date range
            List<StudentVisit> visits = visitRepository.findByVisitDateBetween(startDateTime, endDateTime);
            log.info("Found {} visits in date range", visits.size());

            // Ensure we have visits to analyze
            if (visits.isEmpty()) {
                return createEmptyAnalyticsResponse(startDate, endDate);
            }

            // Calculate visits by day with null safety
            Map<String, Long> visitsByDay = visits.stream()
                    .filter(visit -> visit.getVisitDate() != null)
                    .collect(Collectors.groupingBy(
                            visit -> visit.getVisitDate().format(DateTimeFormatter.ofPattern("EEE")),
                            Collectors.counting()
                    ));

            // Calculate emergencies by day
            Map<String, Long> emergenciesByDay = visits.stream()
                    .filter(visit -> Boolean.TRUE.equals(visit.getEmergencyFlag()) &&
                            visit.getVisitDate() != null)
                    .collect(Collectors.groupingBy(
                            visit -> visit.getVisitDate().format(DateTimeFormatter.ofPattern("EEE")),
                            Collectors.counting()
                    ));

            // Calculate top reasons (using reason field instead of condition)
            Map<String, Long> reasonCounts = visits.stream()
                    .filter(visit -> visit.getReason() != null && !visit.getReason().trim().isEmpty())
                    .collect(Collectors.groupingBy(
                            StudentVisit::getReason,
                            Collectors.counting()
                    ));

            List<Map<String, Object>> topReasons = reasonCounts.entrySet().stream()
                    .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                    .limit(5)
                    .map(entry -> {
                        Map<String, Object> reason = new HashMap<>();
                        reason.put("reason", entry.getKey());
                        reason.put("count", entry.getValue());
                        return reason;
                    })
                    .collect(Collectors.toList());

            // Calculate medications administered with null safety
            Map<String, Long> medicationCounts = visits.stream()
                    .filter(visit -> visit.getMedications() != null)
                    .flatMap(visit -> visit.getMedications().stream())
                    .filter(med -> med.getMedication() != null &&
                            med.getMedication().getMedicationName() != null)
                    .collect(Collectors.groupingBy(
                            med -> med.getMedication().getMedicationName(),
                            Collectors.counting()
                    ));

            List<Map<String, Object>> medicationsAdministered = medicationCounts.entrySet().stream()
                    .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                    .limit(5)
                    .map(entry -> {
                        Map<String, Object> med = new HashMap<>();
                        med.put("medication", entry.getKey());
                        med.put("count", entry.getValue());
                        return med;
                    })
                    .collect(Collectors.toList());

            // Fill in missing days for better chart display
            Map<String, Long> completeVisitsByDay = createCompleteDaysMap(visitsByDay);
            Map<String, Long> completeEmergenciesByDay = createCompleteDaysMap(emergenciesByDay);

            // Create response
            Map<String, Object> analytics = new HashMap<>();
            analytics.put("visitsByDay", completeVisitsByDay);
            analytics.put("emergenciesByDay", completeEmergenciesByDay);
            analytics.put("topReasons", topReasons);
            analytics.put("medicationsAdministered", medicationsAdministered);
            analytics.put("totalVisits", visits.size());
            analytics.put("startDate", startDate.toString());
            analytics.put("endDate", endDate.toString());

            log.info("Analytics calculated successfully");
            return analytics;

        } catch (Exception e) {
            log.error("Error calculating analytics", e);
            return createEmptyAnalyticsResponse(startDate, endDate);
        }
    }

    public List<Map<String, Object>> getAlerts() {
        try {
            log.info("Fetching alerts...");
            List<Map<String, Object>> alerts = new ArrayList<>();

            // Check for low stock medications
            List<MedicationInventory> lowStockMedications = medicationRepository.findLowStockMedications();
            log.info("Found {} low stock medications", lowStockMedications.size());

            for (MedicationInventory med : lowStockMedications) {
                Map<String, Object> alert = new HashMap<>();
                alert.put("id", "low-stock-" + med.getId());
                alert.put("type", "warning");
                alert.put("message", med.getMedicationName() + " running low (" +
                        (med.getCurrentStock() != null ? med.getCurrentStock() : 0) + " units remaining)");
                alert.put("priority", med.getCurrentStock() != null && med.getCurrentStock() < 3 ? "high" : "medium");
                alert.put("timestamp", LocalDateTime.now());
                alert.put("medicationId", med.getId());
                alerts.add(alert);
            }

            // Check for expiring medications (within 30 days)
            LocalDate thirtyDaysFromNow = LocalDate.now().plusDays(30);
            List<MedicationInventory> expiringMedications = medicationRepository.findExpiringMedications(thirtyDaysFromNow);
            log.info("Found {} expiring medications", expiringMedications.size());

            for (MedicationInventory med : expiringMedications) {
                Map<String, Object> alert = new HashMap<>();
                alert.put("id", "expiring-" + med.getId());
                alert.put("type", "warning");
                alert.put("message", med.getMedicationName() + " expires on " +
                        (med.getExpiryDate() != null ? med.getExpiryDate().toString() : "Unknown"));
                alert.put("priority", "medium");
                alert.put("timestamp", LocalDateTime.now());
                alert.put("medicationId", med.getId());
                alerts.add(alert);
            }

            // Check for critical cases from last 24 hours
            LocalDateTime yesterday = LocalDateTime.now().minusHours(24);
            List<StudentVisit> criticalVisits;

            try {
                criticalVisits = visitRepository.findCriticalCases(yesterday);
                log.info("Found {} critical visits", criticalVisits.size());
            } catch (Exception e) {
                log.warn("Error fetching critical cases, using alternative method", e);
                // Fallback: Use findByEmergencyFlagTrue() and filter manually
                criticalVisits = visitRepository.findByEmergencyFlagTrueOrderByVisitDateDesc().stream()
                        .filter(visit -> visit.getVisitDate() != null && visit.getVisitDate().isAfter(yesterday))
                        .collect(Collectors.toList());
                log.info("Found {} critical visits (fallback method)", criticalVisits.size());
            }

            for (StudentVisit visit : criticalVisits) {
                if (visit.getStudent() == null) continue;

                Map<String, Object> alert = new HashMap<>();
                alert.put("id", "critical-" + visit.getId());
                alert.put("type", "urgent");
                alert.put("message",
                        (visit.getStudent().getFirstName() != null ? visit.getStudent().getFirstName() : "") + " " +
                                (visit.getStudent().getLastName() != null ? visit.getStudent().getLastName() : "") + " - " +
                                (visit.getReason() != null ? visit.getReason() : "Unknown reason") + " requires follow-up");
                alert.put("priority", "high");
                alert.put("timestamp", visit.getVisitDate() != null ? visit.getVisitDate() : LocalDateTime.now());
                alert.put("visitId", visit.getId());
                alert.put("studentId", visit.getStudent().getStudentId());
                alerts.add(alert);
            }

            // Sort alerts by priority (high -> medium -> low)
            alerts.sort((a, b) -> {
                String priorityA = (String) a.get("priority");
                String priorityB = (String) b.get("priority");

                // Handle null priorities
                if (priorityA == null) priorityA = "low";
                if (priorityB == null) priorityB = "low";

                // Custom priority order: high > medium > low
                Map<String, Integer> priorityOrder = Map.of("high", 3, "medium", 2, "low", 1);
                int orderA = priorityOrder.getOrDefault(priorityA.toLowerCase(), 0);
                int orderB = priorityOrder.getOrDefault(priorityB.toLowerCase(), 0);

                return Integer.compare(orderB, orderA); // High comes first
            });

            log.info("Total alerts found: {}", alerts.size());
            return alerts;

        } catch (Exception e) {
            log.error("Error fetching alerts", e);
            // Return minimal alert to avoid breaking the dashboard
            Map<String, Object> errorAlert = new HashMap<>();
            errorAlert.put("id", "error-alert");
            errorAlert.put("type", "info");
            errorAlert.put("message", "Unable to load alerts at this time");
            errorAlert.put("priority", "low");
            errorAlert.put("timestamp", LocalDateTime.now());

            return Arrays.asList(errorAlert);
        }
    }

    // === HELPER METHODS ===

    /**
     * Helper method to create a complete days map (Mon-Sun) with all days present
     */
    private Map<String, Long> createCompleteDaysMap(Map<String, Long> existingMap) {
        String[] daysOfWeek = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
        Map<String, Long> completeMap = new LinkedHashMap<>();

        for (String day : daysOfWeek) {
            completeMap.put(day, existingMap.getOrDefault(day, 0L));
        }

        return completeMap;
    }

    /**
     * Helper method to create empty analytics response
     */
    private Map<String, Object> createEmptyAnalyticsResponse(LocalDate startDate, LocalDate endDate) {
        Map<String, Object> analytics = new HashMap<>();
        analytics.put("visitsByDay", createCompleteDaysMap(new HashMap<>()));
        analytics.put("emergenciesByDay", createCompleteDaysMap(new HashMap<>()));
        analytics.put("topReasons", Collections.emptyList());
        analytics.put("medicationsAdministered", Collections.emptyList());
        analytics.put("totalVisits", 0);
        analytics.put("startDate", startDate.toString());
        analytics.put("endDate", endDate.toString());
        return analytics;
    }

    /**
     * Helper method to derive condition from reason (optional - for future use)
     */
    private String deriveConditionFromReason(String reason) {
        if (reason == null || reason.trim().isEmpty()) {
            return "Unknown";
        }

        String lowerReason = reason.toLowerCase();
        if (lowerReason.contains("fever")) {
            return "Fever";
        } else if (lowerReason.contains("headache")) {
            return "Headache";
        } else if (lowerReason.contains("allergy") || lowerReason.contains("reaction")) {
            return "Allergy";
        } else if (lowerReason.contains("injury") || lowerReason.contains("hurt") || lowerReason.contains("cut")) {
            return "Injury";
        } else if (lowerReason.contains("stomach") || lowerReason.contains("pain") || lowerReason.contains("ache")) {
            return "Stomach Pain";
        } else if (lowerReason.contains("cold") || lowerReason.contains("flu") || lowerReason.contains("cough")) {
            return "Respiratory";
        } else if (lowerReason.contains("asthma") || lowerReason.contains("breath")) {
            return "Respiratory";
        } else if (lowerReason.contains("anxiety") || lowerReason.contains("stress")) {
            return "Psychological";
        }

        return "General";
    }
}