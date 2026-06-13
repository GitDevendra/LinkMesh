package com.linkmesh.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "url")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Url {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;  // Snowflake ID — assigned manually

    @Column(name = "short_url", nullable = false, unique = true, length = 16)
    private String shortUrl;

    @Column(name = "long_url", nullable = false)
    private String longUrl;

    @Builder.Default
    @Column(nullable = false)
    private Long clickCount = 0L;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) createdAt = LocalDateTime.now();
    }

    public boolean isExpired() {
        return expiresAt != null && LocalDateTime.now().isAfter(expiresAt);
    }
}