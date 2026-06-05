package com.travelplanner.api.repositories;

import com.travelplanner.api.models.Trip;
import com.travelplanner.api.models.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TripRepository extends JpaRepository<Trip, Long> {

    Page<Trip> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);

    Optional<Trip> findByIdAndUser(Long id, User user);

    long countByUser(User user);
}
