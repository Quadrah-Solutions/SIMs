package com.quadrah.sims.service;

import com.quadrah.sims.model.MedicalHistory;
import com.quadrah.sims.model.Student;
import com.quadrah.sims.repository.MedicalHistoryRepository;
import com.quadrah.sims.repository.StudentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class MedicalHistoryService {

    private final MedicalHistoryRepository medicalHistoryRepository;
    private final StudentRepository studentRepository;

    public MedicalHistoryService(MedicalHistoryRepository medicalHistoryRepository, StudentRepository studentRepository) {
        this.medicalHistoryRepository = medicalHistoryRepository;
        this.studentRepository = studentRepository;
    }

    public List<MedicalHistory> getMedicalHistoryByStudent(Long studentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found with id: " + studentId));
        return medicalHistoryRepository.findByStudentAndIsActiveTrueOrderByConditionName(student);
    }

    public MedicalHistory createMedicalHistory(Long studentId, MedicalHistory medicalHistory) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found with id: " + studentId));

        validateMedicalHistory(medicalHistory);

        medicalHistory.setStudent(student);
        medicalHistory.setIsActive(true);
        return medicalHistoryRepository.save(medicalHistory);
    }

    public MedicalHistory updateMedicalHistory(Long medicalHistoryId, MedicalHistory medicalHistoryDetails) {
        MedicalHistory medicalHistory = medicalHistoryRepository.findById(medicalHistoryId)
                .orElseThrow(() -> new IllegalArgumentException("Medical history not found with id: " + medicalHistoryId));

        validateMedicalHistory(medicalHistoryDetails);

        medicalHistory.setConditionName(medicalHistoryDetails.getConditionName());
        medicalHistory.setDiagnosisDate(medicalHistoryDetails.getDiagnosisDate());
        medicalHistory.setSeverity(medicalHistoryDetails.getSeverity());
        medicalHistory.setTreatment(medicalHistoryDetails.getTreatment());
        medicalHistory.setNotes(medicalHistoryDetails.getNotes());

        return medicalHistoryRepository.save(medicalHistory);
    }

    public void deactivateMedicalHistory(Long medicalHistoryId) {
        MedicalHistory medicalHistory = medicalHistoryRepository.findById(medicalHistoryId)
                .orElseThrow(() -> new IllegalArgumentException("Medical history not found with id: " + medicalHistoryId));

        medicalHistory.setIsActive(false);
        medicalHistoryRepository.save(medicalHistory);
    }

    public boolean studentHasCondition(Long studentId, String conditionName) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found with id: " + studentId));
        return medicalHistoryRepository.existsByStudentAndConditionNameContainingIgnoreCaseAndIsActiveTrue(student, conditionName);
    }

    public List<Student> getStudentsWithCondition(String conditionName) {
        return medicalHistoryRepository.findStudentsByConditionName(conditionName);
    }

    // NEW METHOD: Get Medical History Summary Report
    public Map<String, Object> getMedicalHistorySummary(ZonedDateTime startDate, ZonedDateTime endDate) {
        Map<String, Object> summary = new HashMap<>();

        // Get total cases in date range
        Long totalCases = medicalHistoryRepository.countByDiagnosisDateBetweenAndIsActiveTrue(startDate, endDate);
        summary.put("totalCases", totalCases);

        // Get most common conditions
        List<Object[]> commonConditions = medicalHistoryRepository.findCommonConditionsByDateRange(startDate, endDate);
        summary.put("commonConditions", commonConditions);

        // Get cases by severity
        List<Object[]> casesBySeverity = medicalHistoryRepository.countCasesBySeverityAndDateRange(startDate, endDate);
        summary.put("casesBySeverity", casesBySeverity);

        // Get new cases count (first diagnosis in this period)
        Long newCases = medicalHistoryRepository.countNewCasesInPeriod(startDate, endDate);
        summary.put("newCases", newCases);

        // Get cases by month/week (for charting)
        List<Object[]> casesOverTime = medicalHistoryRepository.countCasesOverTime(startDate, endDate);
        summary.put("casesOverTime", casesOverTime);

        summary.put("startDate", startDate);
        summary.put("endDate", endDate);

        return summary;
    }

    private void validateMedicalHistory(MedicalHistory medicalHistory) {
        if (medicalHistory.getConditionName() == null || medicalHistory.getConditionName().trim().isEmpty()) {
            throw new IllegalArgumentException("Condition name is required.");
        }
    }
}