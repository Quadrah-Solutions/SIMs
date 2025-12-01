package com.quadrah.sims.service;

import com.quadrah.sims.repository.StudentVisitRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ReportingService {

    private final StudentVisitRepository visitRepository;

    public ReportingService(StudentVisitRepository visitRepository) {
        this.visitRepository = visitRepository;
    }

    public Map<String, Object> getDashboardStatistics(LocalDateTime startDate, LocalDateTime endDate) {
        if (startDate == null) {
            startDate = LocalDateTime.now().minusMonths(1); // Default to last 30 days
        }
        if (endDate == null) {
            endDate = LocalDateTime.now();
        }

        Object[] stats = visitRepository.getVisitStatistics(startDate, endDate);
        List<Object[]> topIssues = visitRepository.getTopHealthIssues(startDate, endDate);
        List<Object[]> monthlyTrends = visitRepository.getMonthlyVisitTrends(startDate, endDate);

        Map<String, Object> dashboardStats = new HashMap<>();

        if (stats != null && stats.length >= 4) {
            dashboardStats.put("totalVisits", stats[0]);
            dashboardStats.put("emergencyVisits", stats[1]);
            dashboardStats.put("sentHome", stats[2]);
            dashboardStats.put("returnedToClass", stats[3]);
        }

        dashboardStats.put("topHealthIssues", topIssues);
        dashboardStats.put("monthlyTrends", monthlyTrends);
        dashboardStats.put("activeObservations", visitRepository.findByDispositionIsNullOrderByVisitDateDesc().size());

        return dashboardStats;
    }

    public Map<String, Long> getVisitStatisticsByDisposition(LocalDateTime startDate, LocalDateTime endDate) {
        List<Object[]> results = visitRepository.countVisitsByDispositionBetweenDates(startDate, endDate);

        Map<String, Long> statistics = new HashMap<>();
        for (Object[] result : results) {
            if (result[0] != null && result[1] != null) {
                statistics.put(result[0].toString(), (Long) result[1]);
            }
        }

        return statistics;
    }

    public List<Object[]> getFrequentVisitorsReport(LocalDateTime startDate, LocalDateTime endDate, int minVisits) {
        return visitRepository.getFrequentVisitors(startDate, endDate, (long) minVisits);
    }

    public List<Object[]> getMedicationUsageReport(LocalDateTime startDate, LocalDateTime endDate) {
        // This would use a method from MedicationAdministrationRepository
        // For now, return empty list - you can implement this later
        return List.of();
    }
}