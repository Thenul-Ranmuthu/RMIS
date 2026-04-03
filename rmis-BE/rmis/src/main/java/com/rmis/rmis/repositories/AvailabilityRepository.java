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

    List<Availability> findByTechnicianIdAndDateGreaterThanEqualAndStatusOrderByDateAscStartTimeAsc(
            Long technicianId, LocalDate fromDate, String status);

    List<Availability> findByTechnicianIdAndDateAndStatusOrderByStartTimeAsc(
            Long technicianId, LocalDate date, String status);

    boolean existsByTechnicianIdAndDateAndStartTimeLessThanEqualAndEndTimeGreaterThanEqual(
            Long technicianId,
            LocalDate date,
            java.time.LocalTime endTime,
            java.time.LocalTime startTime);
}