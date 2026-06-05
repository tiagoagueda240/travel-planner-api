package com.travelplanner.api.repositories;

import com.travelplanner.api.models.Place;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PlaceRepository extends JpaRepository<Place, Long> {

    Optional<Place> findByIdAndDayId(Long id, Long dayId);
}
