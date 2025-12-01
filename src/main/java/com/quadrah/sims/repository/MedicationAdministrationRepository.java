package com.quadrah.sims.repository;

import com.quadrah.sims.model.MedicationAdministration;
import com.quadrah.sims.model.StudentVisit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MedicationAdministrationRepository extends JpaRepository<MedicationAdministration, Long> {

    // Find all medication administrations for a visit
    List<MedicationAdministration> findByStudentVisitOrderByAdministrationTimeDesc(StudentVisit visit);

    // Find medication administrations by medication name
    List<MedicationAdministration> findByMedicationNameContainingIgnoreCaseOrderByAdministrationTimeDesc(String medicationName);

    // Find medication administrations within a date range
    List<MedicationAdministration> findByAdministrationTimeBetweenOrderByAdministrationTimeDesc(LocalDateTime startDate, LocalDateTime endDate);

    // Find medication administrations for a specific student (via visit)
    @Query("SELECT ma FROM MedicationAdministration ma WHERE ma.studentVisit.student.id = :studentId ORDER BY ma.administrationTime DESC")
    List<MedicationAdministration> findByStudentId(@Param("studentId") Long studentId);

    // Count medication administrations by medication for reporting
    @Query("SELECT ma.medicationName, COUNT(ma) FROM MedicationAdministration ma WHERE ma.administrationTime BETWEEN :startDate AND :endDate GROUP BY ma.medicationName")
    List<Object[]> countAdministrationsByMedicationBetweenDates(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    // Find recent medication administrations
    List<MedicationAdministration> findTop10ByOrderByAdministrationTimeDesc();
}
