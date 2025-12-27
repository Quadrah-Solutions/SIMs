package com.quadrah.sims.repository;

import com.quadrah.sims.model.Student;
import com.quadrah.sims.model.StudentVisit;
import com.quadrah.sims.model.UserAccount;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface StudentVisitRepository extends JpaRepository<StudentVisit, Long> {

    // Existing methods
    List<StudentVisit> findByStudentOrderByVisitDateDesc(Student student);
    List<StudentVisit> findByNurseOrderByVisitDateDesc(UserAccount nurse);
    List<StudentVisit> findByDispositionOrderByVisitDateDesc(StudentVisit.DispositionType disposition);
    List<StudentVisit> findByEmergencyFlagTrueOrderByVisitDateDesc();
    List<StudentVisit> findByVisitDateBetweenOrderByVisitDateDesc(LocalDateTime startDate, LocalDateTime endDate);
    List<StudentVisit> findByStudentAndVisitDateBetweenOrderByVisitDateDesc(Student student, LocalDateTime startDate, LocalDateTime endDate);
    List<StudentVisit> findByDispositionIsNullOrderByVisitDateDesc();
    long countByStudent(Student student);
    List<StudentVisit> findByStudentInOrderByVisitDateDesc(List<Student> students);

    // Add these reporting queries
    @Query("SELECT " +
            "COUNT(v) as totalVisits, " +
            "SUM(CASE WHEN v.emergencyFlag = true THEN 1 ELSE 0 END) as emergencyVisits, " +
            "SUM(CASE WHEN v.disposition = 'SENT_HOME' THEN 1 ELSE 0 END) as sentHome, " +
            "SUM(CASE WHEN v.disposition = 'RETURNED_TO_CLASS' THEN 1 ELSE 0 END) as returnedToClass " +
            "FROM StudentVisit v WHERE v.visitDate BETWEEN :startDate AND :endDate")
    Object[] getVisitStatistics(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query(value = """
        WITH word_counts AS (
            SELECT regexp_split_to_table(LOWER(reason), '\\s+') as word 
            FROM student_visits 
            WHERE visit_date BETWEEN :startDate AND :endDate AND reason IS NOT NULL
        )
        SELECT word, COUNT(*) as frequency 
        FROM word_counts 
        WHERE length(word) > 3 
        GROUP BY word 
        ORDER BY frequency DESC 
        LIMIT 10
        """, nativeQuery = true)
    List<Object[]> getTopHealthIssues(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT FUNCTION('DATE_TRUNC', 'month', v.visitDate), COUNT(v) " +
            "FROM StudentVisit v " +
            "WHERE v.visitDate BETWEEN :startDate AND :endDate " +
            "GROUP BY FUNCTION('DATE_TRUNC', 'month', v.visitDate) " +
            "ORDER BY FUNCTION('DATE_TRUNC', 'month', v.visitDate)")
    List<Object[]> getMonthlyVisitTrends(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT v.student, COUNT(v) as visitCount " +
            "FROM StudentVisit v " +
            "WHERE v.visitDate BETWEEN :startDate AND :endDate " +
            "GROUP BY v.student " +
            "HAVING COUNT(v) >= :minVisits " +
            "ORDER BY visitCount DESC")
    List<Object[]> getFrequentVisitors(@Param("startDate") LocalDateTime startDate,
                                       @Param("endDate") LocalDateTime endDate,
                                       @Param("minVisits") Long minVisits);

    // Count visits by disposition for statistics
    @Query("SELECT v.disposition, COUNT(v) FROM StudentVisit v WHERE v.visitDate BETWEEN :startDate AND :endDate GROUP BY v.disposition")
    List<Object[]> countVisitsByDispositionBetweenDates(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    // Find recent visits (last 30 days)
    @Query("SELECT v FROM StudentVisit v WHERE v.visitDate >= :date ORDER BY v.visitDate DESC")
    List<StudentVisit> findRecentVisits(@Param("date") LocalDateTime date);


    @Query(value = "SELECT COUNT(DISTINCT s.id) FROM StudentVisit v JOIN v.student s WHERE v.visitDate BETWEEN :startDate AND :endDate")
    Object[] countUniqueStudentsBetweenDates(@Param("startDate") LocalDateTime startDate,
                                             @Param("endDate") LocalDateTime endDate);

    @Query(value = "SELECT COUNT(m) FROM StudentVisit v JOIN v.medications m WHERE v.visitDate BETWEEN :startDate AND :endDate")
    Object[] countMedicationsAdministeredBetweenDates(@Param("startDate") LocalDateTime startDate,
                                                      @Param("endDate") LocalDateTime endDate);

    // Count visits between dates
    Long countByVisitDateBetween(LocalDateTime startDate, LocalDateTime endDate);

    // FIXED: Changed from countByVisitDateBetweenAndIsEmergencyTrue to countByVisitDateBetweenAndEmergencyFlagTrue
    Long countByVisitDateBetweenAndEmergencyFlagTrue(LocalDateTime startDate, LocalDateTime endDate);

    // Count visits with specific health issue
    @Query("SELECT COUNT(v) FROM StudentVisit v WHERE v.visitDate BETWEEN :startDate AND :endDate " +
            "AND (LOWER(v.reason) LIKE LOWER(CONCAT('%', :healthIssue, '%')) " +
            "OR LOWER(v.symptoms) LIKE LOWER(CONCAT('%', :healthIssue, '%')) " +
            "OR LOWER(v.observations) LIKE LOWER(CONCAT('%', :healthIssue, '%')))")
    Long countByVisitDateBetweenAndHealthIssueContaining(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("healthIssue") String healthIssue);

    // Get visit summary by type (combined query)
    @Query("SELECT 'Routine checks' as visitType, COUNT(v) as visitCount FROM StudentVisit v " +
            "WHERE v.visitDate BETWEEN :startDate AND :endDate AND v.emergencyFlag = false " +
            "UNION ALL " +
            "SELECT 'Emergencies', COUNT(v) FROM StudentVisit v " +
            "WHERE v.visitDate BETWEEN :startDate AND :endDate AND v.emergencyFlag = true " +
            "UNION ALL " +
            "SELECT 'Injuries', COUNT(v) FROM StudentVisit v " +
            "WHERE v.visitDate BETWEEN :startDate AND :endDate " +
            "AND (LOWER(v.reason) LIKE '%injury%' OR LOWER(v.reason) LIKE '%injured%') " +
            "UNION ALL " +
            "SELECT 'Illness', COUNT(v) FROM StudentVisit v " +
            "WHERE v.visitDate BETWEEN :startDate AND :endDate " +
            "AND (LOWER(v.reason) LIKE '%fever%' OR LOWER(v.reason) LIKE '%cold%' " +
            "OR LOWER(v.reason) LIKE '%flu%' OR LOWER(v.reason) LIKE '%headache%')")
    List<Object[]> getVisitSummaryByType(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);


    // Get unique student count


    // For change percentage calculations - get count for previous period
    @Query("SELECT COUNT(v) FROM StudentVisit v WHERE v.visitDate BETWEEN :startDate AND :endDate")
    Long countVisitsInPeriod(@Param("startDate") LocalDateTime startDate,
                             @Param("endDate") LocalDateTime endDate);

    @Query("SELECT COUNT(v) FROM StudentVisit v WHERE v.visitDate BETWEEN :startDate AND :endDate AND v.emergencyFlag = true")
    Long countEmergencyVisitsInPeriod(@Param("startDate") LocalDateTime startDate,
                                      @Param("endDate") LocalDateTime endDate);

    @Query("SELECT COUNT(v) FROM StudentVisit v WHERE v.visitDate BETWEEN :startDate AND :endDate AND v.disposition = 'SENT_HOME'")
    Long countSentHomeInPeriod(@Param("startDate") LocalDateTime startDate,
                               @Param("endDate") LocalDateTime endDate);

    @Query("SELECT COUNT(v) FROM StudentVisit v WHERE v.visitDate BETWEEN :startDate AND :endDate AND v.disposition = 'RETURNED_TO_CLASS'")
    Long countReturnedToClassInPeriod(@Param("startDate") LocalDateTime startDate,
                                      @Param("endDate") LocalDateTime endDate);

    Page<StudentVisit> findAll(Specification<StudentVisit> spec, Pageable pageable);
}