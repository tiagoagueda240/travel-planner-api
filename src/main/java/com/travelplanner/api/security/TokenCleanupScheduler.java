package com.travelplanner.api.security;

import com.travelplanner.api.repositories.TokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class TokenCleanupScheduler {

    private final TokenRepository tokenRepository;

    /**
     * Runs every day at 03:00 AM and deletes all expired/revoked tokens from the DB.
     * Prevents the tokens table from growing indefinitely.
     */
    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void purgeExpiredTokens() {
        int deleted = tokenRepository.deleteExpiredAndRevokedTokens();
        log.info("Token cleanup: {} expired/revoked tokens removed", deleted);
    }
}
