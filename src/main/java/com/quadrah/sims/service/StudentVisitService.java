package com.quadrah.sims.service;

import com.quadrah.sims.model.Student;
import com.quadrah.sims.model.StudentVisit;
import com.quadrah.sims.model.UserAccount;
import com.quadrah.sims.repository.StudentRepository;
import com.quadrah.sims.repository.StudentVisitRepository;
import com.quadrah.sims.repository.UserAccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class StudentVisitService {

    private final StudentVisitRepository visitRepository;
    private final StudentRepository studentRepository;
    private final UserAccountRepository userAccountRepository;
    private final NotificationService notificationService;

    public StudentVisitService(StudentVisitRepository visitRepository,
                               StudentRepository studentRepository,
                               UserAccountRepository userAccountRepository,
                               NotificationService notificationService) {
        this.visitRepository = visitRepository;
        this.studentRepository = studentRepository;
        this.userAccountRepository = userAccountRepository;
        this.notificationService = notificationService;
    }

    public List<StudentVisit> getAllVisits() {
        return visitRepository.findAll();
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

    public StudentVisit createVisit(StudentVisit visit) {
        validateVisit(visit);

        // Set visit date to now if not provided
        if (visit.getVisitDate() == null) {
            visit.setVisitDate(LocalDateTime.now());
        }

        StudentVisit savedVisit = visitRepository.save(visit);

        // Handle emergency notifications
        if (Boolean.TRUE.equals(visit.getEmergencyFlag())) {
            notificationService.notifyEmergencyVisit(savedVisit);
        }

        return savedVisit;
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
}
