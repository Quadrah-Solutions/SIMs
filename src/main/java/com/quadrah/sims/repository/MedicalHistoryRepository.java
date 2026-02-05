package com.quadrah.sims.repository;

import com.quadrah.sims.model.MedicalHistory;
import com.quadrah.sims.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.ZonedDateTime;
import java.util.List;

@Repository
public interface MedicalHistoryRepository extends JpaRepository<MedicalHistory, Long> {

    // Find medical history for a student, ordered by condition name
    List<MedicalHistory> findByStudentOrderByConditionName(Student student);

    // Find active medical history for a student
    List<MedicalHistory> findByStudentAndIsActiveTrueOrderByConditionName(Student student);

    // Find medical history by condition name (across all students)
    List<MedicalHistory> findByConditionNameContainingIgnoreCase(String conditionName);

    // Find students with specific medical conditions
    @Query("SELECT m.student FROM MedicalHistory m WHERE LOWER(m.conditionName) LIKE LOWER(CONCAT('%', :conditionName, '%')) AND m.isActive = true")
    List<Student> findStudentsByConditionName(@Param("conditionName") String conditionName);

    // Count medical conditions by type for reporting
    @Query("SELECT m.conditionName, COUNT(m) FROM MedicalHistory m WHERE m.isActive = true GROUP BY m.conditionName")
    List<Object[]> countConditionsByType();

    // Check if student has a specific medical condition
    boolean existsByStudentAndConditionNameContainingIgnoreCaseAndIsActiveTrue(Student student, String conditionName);

    // === NEW METHODS FOR SUMMARY REPORT ===

    // Count total cases within date range
    Long countByDiagnosisDateBetweenAndIsActiveTrue(ZonedDateTime startDate, ZonedDateTime endDate);

    // Find most common conditions within date range
    @Query("SELECT m.conditionName, COUNT(m) as count FROM MedicalHistory m " +
            "WHERE m.diagnosisDate BETWEEN :startDate AND :endDate AND m.isActive = true " +
            "GROUP BY m.conditionName ORDER BY count DESC")
    List<Object[]> findCommonConditionsByDateRange(@Param("startDate") ZonedDateTime startDate,
                                                   @Param("endDate") ZonedDateTime endDate);

    // Count cases by severity within date range
    @Query("SELECT m.severity, COUNT(m) FROM MedicalHistory m " +
            "WHERE m.diagnosisDate BETWEEN :startDate AND :endDate AND m.isActive = true " +
            "GROUP BY m.severity")
    List<Object[]> countCasesBySeverityAndDateRange(@Param("startDate") ZonedDateTime startDate,
                                                    @Param("endDate") ZonedDateTime endDate);

    // Count new cases (first diagnosis) in period
    @Query("SELECT COUNT(DISTINCT m.student) FROM MedicalHistory m " +
            "WHERE m.diagnosisDate BETWEEN :startDate AND :endDate AND m.isActive = true " +
            "AND NOT EXISTS (SELECT m2 FROM MedicalHistory m2 WHERE m2.student = m.student " +
            "AND m2.conditionName = m.conditionName AND m2.diagnosisDate < :startDate)")
    Long countNewCasesInPeriod(@Param("startDate") ZonedDateTime startDate,
                               @Param("endDate") ZonedDateTime endDate);

    // Count cases over time (daily) for charting
    @Query("SELECT DATE(m.diagnosisDate) as date, COUNT(m) FROM MedicalHistory m " +
            "WHERE m.diagnosisDate BETWEEN :startDate AND :endDate AND m.isActive = true " +
            "GROUP BY DATE(m.diagnosisDate) ORDER BY date")
    List<Object[]> countCasesOverTime(@Param("startDate") ZonedDateTime startDate,
                                      @Param("endDate") ZonedDateTime endDate);

    // Alternative method for PostgreSQL (if DATE() function doesn't work)
    @Query(value = "SELECT CAST(m.diagnosis_date AS DATE) as date, COUNT(*) FROM medical_history m " +
            "WHERE m.diagnosis_date BETWEEN :startDate AND :endDate AND m.is_active = true " +
            "GROUP BY CAST(m.diagnosis_date AS DATE) ORDER BY date", nativeQuery = true)
    List<Object[]> countCasesOverTimeNative(@Param("startDate") ZonedDateTime startDate,
                                            @Param("endDate") ZonedDateTime endDate);

    // Get summary statistics (min, max, avg diagnosis dates)
    @Query("SELECT MIN(m.diagnosisDate), MAX(m.diagnosisDate), COUNT(m) FROM MedicalHistory m " +
            "WHERE m.diagnosisDate BETWEEN :startDate AND :endDate AND m.isActive = true")
    Object[] getDateRangeStatistics(@Param("startDate") ZonedDateTime startDate,
                                    @Param("endDate") ZonedDateTime endDate);

    // Find all active medical history within date range
    List<MedicalHistory> findByDiagnosisDateBetweenAndIsActiveTrueOrderByDiagnosisDateDesc(ZonedDateTime startDate,
                                                                                           ZonedDateTime endDate);
}