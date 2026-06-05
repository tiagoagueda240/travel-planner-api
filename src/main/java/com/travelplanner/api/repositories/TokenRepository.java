package com.travelplanner.api.repositories;

import com.travelplanner.api.models.Token;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TokenRepository extends JpaRepository<Token, Integer> {

    @Query("""
            SELECT t FROM Token t
            WHERE t.user.id = :userId
            AND (t.expired = false OR t.revoked = false)
            """)
    List<Token> findAllValidTokensByUser(Integer userId);

    Optional<Token> findByToken(String token);

    @Modifying
    @Query("DELETE FROM Token t WHERE t.user.id = :userId")
    void deleteAllByUserId(Integer userId);

    @Modifying
    @Query("DELETE FROM Token t WHERE t.expired = true OR t.revoked = true")
    int deleteExpiredAndRevokedTokens();
}
