package com.travelplanner.api.repositories;

import com.travelplanner.api.models.Day;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DayRepository extends JpaRepository<Day, Long> {

    Optional<Day> findByIdAndTripId(Long id, Long tripId);
}
