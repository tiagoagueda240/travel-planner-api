package com.travelplanner.api.repositories;

import com.travelplanner.api.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
  // Metodo que o Spring Data JPA vai implementar automaticamente para encontrar utilizadores pelo email
  Optional<User> findByEmail(String email);
}
