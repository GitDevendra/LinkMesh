package com.linkmesh.service;

import com.linkmesh.dto.UrlDto;
import com.linkmesh.dto.UserUrlResponse;
import com.linkmesh.entity.Url;
import com.linkmesh.entity.User;
import com.linkmesh.exception.AliasAlreadyExistsException;
import com.linkmesh.exception.PremiumRequiredException;
import com.linkmesh.exception.UrlExpiredException;
import com.linkmesh.exception.UrlNotFoundException;
import com.linkmesh.repository.UrlRepository;
import com.linkmesh.util.Base62Encoder;
import com.linkmesh.util.SnowflakeIdGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UrlService {

    private final UrlRepository urlRepository;
    private final SnowflakeIdGenerator snowflake;
    private final CustomOAuth2UserService customOAuth2UserService;

    @Value("${app.base-url}")
    private String baseUrl;

    @Value("${app.short-url-length:7}")
    private int shortUrlLength;

    @Value("${app.default-expiry-days:365}")
    private int defaultExpiryDays;

    // ── Create ────────────────────────────────────────────────

    @Transactional
    public UrlDto.CreateResponse createShortUrl(UrlDto.CreateRequest request) {

        User user = customOAuth2UserService.getCurrentUser();

        // --- Premium gate ---
        boolean hasCustomAlias = request.customAlias() != null && !request.customAlias().isBlank();
        boolean hasCustomExpiry = request.expiryDays() != null && request.expiryDays() != defaultExpiryDays;

        if (hasCustomAlias && !user.isPremium()) {
            throw new PremiumRequiredException("Custom Alias");
        }
        if (hasCustomExpiry && !user.isPremium()) {
            throw new PremiumRequiredException("Custom Expiry");
        }

        // Determine short code
        String shortCode;
        if (hasCustomAlias) {
            shortCode = request.customAlias();
            if (urlRepository.existsByShortUrl(shortCode)) {
                throw new AliasAlreadyExistsException(shortCode);
            }
        } else {
            shortCode = generateUniqueShortCode();
        }

        // Expiry — FREE users always get the default
        int ttlDays = (request.expiryDays() != null && user.isPremium())
                ? request.expiryDays()
                : defaultExpiryDays;
        LocalDateTime expiresAt = LocalDateTime.now().plusDays(ttlDays);

        // Persist
        Url url = Url.builder()
                .id(snowflake.nextId())
                .shortUrl(shortCode)
                .longUrl(request.longUrl())
                .user(user)
                .expiresAt(expiresAt)
                .build();

        try {
            urlRepository.save(url);
        } catch (DataIntegrityViolationException ex) {
            log.warn("Collision on shortCode={} — retrying", shortCode);
            shortCode = generateUniqueShortCode();
            url = Url.builder()
                    .id(snowflake.nextId())
                    .shortUrl(shortCode)
                    .longUrl(request.longUrl())
                    .user(user)
                    .expiresAt(expiresAt)
                    .build();
            urlRepository.save(url);
        }

        log.info("Created: {} → {} (user={}, role={})", shortCode, request.longUrl(),
                user.getEmail(), user.getRole());

        return new UrlDto.CreateResponse(
                baseUrl + "/v1/url/" + shortCode,
                request.longUrl(),
                url.getCreatedAt(),
                expiresAt,
                "Short URL created successfully"
        );
    }

    @Transactional(readOnly = true)
    public List<UserUrlResponse> getMyUrls() {
        User user = customOAuth2UserService.getCurrentUser();
        return urlRepository.findAllByUserId(user.getId())
                .stream()
                .map(url -> new UserUrlResponse(
                        url.getShortUrl(),
                        url.getLongUrl(),
                        url.getClickCount(),
                        url.getCreatedAt(),
                        url.getExpiresAt()
                ))
                .toList();
    }

    // ── Resolve ───────────────────────────────────────────────

    @Cacheable(value = "urls", key = "#shortUrl", unless = "#result == null")
    @Transactional(readOnly = true)
    public String getLongUrl(String shortUrl) {
        Url url = urlRepository.findByShortUrl(shortUrl)
                .orElseThrow(() -> new UrlNotFoundException(shortUrl));
        if (url.isExpired()) throw new UrlExpiredException(shortUrl);
        return url.getLongUrl();
    }

    // ── Click count ───────────────────────────────────────────

    @Transactional
    public void incrementClickCount(String shortUrl) {
        urlRepository.incrementClickCount(shortUrl);
    }

    // ── Cache eviction ────────────────────────────────────────

    @CacheEvict(value = "urls", key = "#shortUrl")
    public void evictFromCache(String shortUrl) {
        log.debug("Cache evicted for: {}", shortUrl);
    }

    // ── Delete ────────────────────────────────────────────────

    @Transactional
    public void deleteUrl(String shortUrl) {
        User currentUser = customOAuth2UserService.getCurrentUser();
        Url url = urlRepository.findByShortUrl(shortUrl)
                .orElseThrow(() -> new UrlNotFoundException(shortUrl));
        if (url.getUser() == null || !url.getUser().getId().equals(currentUser.getId())) {
            throw new RuntimeException("You are not the owner of this URL");
        }
        urlRepository.delete(url);
        evictFromCache(shortUrl);
    }

    // ── Helper ────────────────────────────────────────────────

    private String generateUniqueShortCode() {
        for (int attempt = 0; attempt < 3; attempt++) {
            String code = Base62Encoder.encodeFixedLength(snowflake.nextId(), shortUrlLength);
            if (!urlRepository.existsByShortUrl(code)) return code;
            log.warn("Short code collision attempt {}: {}", attempt + 1, code);
        }
        throw new IllegalStateException("Failed to generate unique short code after 3 attempts");
    }
}