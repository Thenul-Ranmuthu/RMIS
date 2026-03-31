package com.rmis.rmis.repositories;

import com.rmis.rmis.domain.entities.Availability;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface AvailabilityRepository extends JpaRepository<Availability, Long> {

    List<Availability> findByTechnicianIdAndDateGreaterThanEqualOrderByDateAscStartTimeAsc(
            Long technicianId, LocalDate fromDate);

    boolean existsByTechnicianIdAndDateAndStartTimeLessThanEqualAndEndTimeGreaterThanEqual(
            Long technicianId,
            LocalDate date,
            java.time.LocalTime endTime,
            java.time.LocalTime startTime);
}