package com.quadrah.sims.controller;

import com.quadrah.sims.service.ReportingService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/reports")
public class ReportingController {

    private final ReportingService reportingService;

    public ReportingController(ReportingService reportingService) {
        this.reportingService = reportingService;
    }

    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('NURSE') or hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getDashboardStatistics(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

        Map<String, Object> statistics = reportingService.getDashboardStatistics(startDate, endDate);
        return ResponseEntity.ok(statistics);
    }

    @GetMapping("/visits-by-disposition")
    @PreAuthorize("hasRole('NURSE') or hasRole('ADMIN')")
    public ResponseEntity<Map<String, Long>> getVisitStatisticsByDisposition(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

        Map<String, Long> statistics = reportingService.getVisitStatisticsByDisposition(startDate, endDate);
        return ResponseEntity.ok(statistics);
    }

    @GetMapping("/frequent-visitors")
    @PreAuthorize("hasRole('NURSE') or hasRole('ADMIN')")
    public ResponseEntity<?> getFrequentVisitorsReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(defaultValue = "3") int minVisits) {

        var report = reportingService.getFrequentVisitorsReport(startDate, endDate, minVisits);
        return ResponseEntity.ok(report);
    }

    @GetMapping("/medication-usage")
    @PreAuthorize("hasRole('NURSE') or hasRole('ADMIN')")
    public ResponseEntity<?> getMedicationUsageReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

        var report = reportingService.getMedicationUsageReport(startDate, endDate);
        return ResponseEntity.ok(report);
    }

    @GetMapping("/compliance-issues")
    @PreAuthorize("hasRole('NURSE') or hasRole('ADMIN')")
    public ResponseEntity<List<Map<String, Object>>> getComplianceIssues(
            @RequestParam(required = false) String grade,
            @RequestParam(required = false) String status) {

        List<Map<String, Object>> issues = reportingService.getComplianceIssues(grade, status);
        return ResponseEntity.ok(issues);
    }


    // NEW: Add trends endpoint
    @GetMapping("/trends")
    @PreAuthorize("hasRole('NURSE') or hasRole('ADMIN')")
    public ResponseEntity<List<Map<String, Object>>> getTrendData(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

        List<Map<String, Object>> trends = reportingService.getTrendData(startDate, endDate);
        return ResponseEntity.ok(trends);
    }

    // NEW: Add visit summary by type
    @GetMapping("/visit-summary")
    @PreAuthorize("hasRole('NURSE') or hasRole('ADMIN')")
    public ResponseEntity<List<Map<String, Object>>> getVisitSummary(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

        List<Map<String, Object>> summary = reportingService.getVisitSummary(startDate, endDate);
        return ResponseEntity.ok(summary);
    }

    @GetMapping("/conditions")
    public ResponseEntity<List<String>> getMedicalConditions() {
        try {
            // Return hardcoded list for now
            List<String> conditions = Arrays.asList(
                    "Asthma",
                    "Allergy",
                    "Diabetes",
                    "Epilepsy",
                    "Hypertension",
                    "Migraine",
                    "Anemia",
                    "Arthritis"
            );

            return ResponseEntity.ok(conditions);
        } catch (Exception e) {
//            logger.error("Error fetching conditions", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}