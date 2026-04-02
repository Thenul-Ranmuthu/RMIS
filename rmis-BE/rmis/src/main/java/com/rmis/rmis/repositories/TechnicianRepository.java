package com.rmis.rmis.repositories;

import com.rmis.rmis.domain.entities.Technician;
import com.rmis.rmis.domain.enums.SkillLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface TechnicianRepository extends JpaRepository<Technician, Long> {

    Optional<Technician> findByEmail(String email);

    boolean existsByEmail(String email);

    List<Technician> findByStatus(String status);

    @Query("""
        SELECT DISTINCT t FROM Technician t
        JOIN t.availabilities a
        WHERE t.status = 'ACTIVE'
        AND a.date = :date
        AND a.status = 'AVAILABLE'
        AND (:skillLevel IS NULL OR t.skillLevel = :skillLevel)
        ORDER BY t.firstName ASC
    """)
    List<Technician> findAvailableByDateAndSkillLevel(
            @Param("date") LocalDate date,
            @Param("skillLevel") SkillLevel skillLevel
    );

    @Query("""
    SELECT DISTINCT t FROM Technician t
    WHERE t.status = 'ACTIVE'
    AND (:skillLevel IS NULL OR t.skillLevel = :skillLevel)
    ORDER BY t.firstName ASC
    """)
    List<Technician> findActiveBySkillLevel(@Param("skillLevel") SkillLevel skillLevel);
}