package com.quadrah.sims.repository;

import com.quadrah.sims.model.ClassRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClassRoomRepository extends JpaRepository<ClassRoom, Long> {

    // Find all classrooms ordered by name
    List<ClassRoom> findAllByOrderByClassNameAsc();

    // Find classrooms by grade
    List<ClassRoom> findByGradeIdOrderByClassNameAsc(Long gradeId);

    // Find classrooms by grade name
    @Query("SELECT c FROM ClassRoom c JOIN c.grade g WHERE g.gradeName = :gradeName ORDER BY c.className")
    List<ClassRoom> findByGradeName(String gradeName);

    // Get just classroom names (for dropdowns)
    @Query("SELECT c.className FROM ClassRoom c ORDER BY c.className")
    List<String> findAllClassNames();

    // Find classroom by exact name
    ClassRoom findByClassName(String className);
}
