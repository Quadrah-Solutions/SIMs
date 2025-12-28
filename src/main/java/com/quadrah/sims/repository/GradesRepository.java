package com.quadrah.sims.repository;

import com.quadrah.sims.model.Grade;
import com.quadrah.sims.model.ClassRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GradesRepository extends JpaRepository<Grade, Long> {

    // Find all grades ordered by sortOrder or name
    List<Grade> findAllByOrderBySortOrderAsc();

    // Find all grades by name containing (for search)
    List<Grade> findByGradeNameContainingIgnoreCase(String name);

    // Find grade by exact name
    Grade findByGradeName(String gradeName);

    // Get just grade names (for dropdowns)
    @Query("SELECT g.gradeName FROM Grade g ORDER BY g.sortOrder ASC, g.gradeName ASC")
    List<String> findAllGradeNames();
}

