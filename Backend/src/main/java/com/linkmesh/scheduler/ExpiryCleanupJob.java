package com.linkmesh.scheduler;

import com.linkmesh.repository.UrlRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExpiryCleanupJob {

    private final UrlRepository urlRepository;

    @Transactional
    @Scheduled(cron = "0 0 2 * * *", zone = "UTC")
    public void deleteExpiredUrls() {
        LocalDateTime now = LocalDateTime.now();
        log.info("[LinkMesh Cleanup] Starting expired URL delete at {}", now);
        try {
            int count = urlRepository.deleteExpiredUrls(now);
            log.info("[LinkMesh Cleanup] Deleted {} expired URL(s)", count);
        } catch (Exception ex) {
            log.error("[LinkMesh Cleanup] Delete failed", ex);
        }
    }
}