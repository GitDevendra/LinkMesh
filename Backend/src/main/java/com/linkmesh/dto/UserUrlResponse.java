package com.linkmesh.dto;

import java.time.LocalDateTime;

public record UserUrlResponse(
        String shortUrl,
        String longUrl,
        Long clickCount,
        LocalDateTime createdAt,
        LocalDateTime expiresAt
) {}
