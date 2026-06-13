package com.linkmesh.repository;

import com.linkmesh.entity.Url;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UrlRepository extends JpaRepository<Url, Long> {

    Optional<Url> findByShortUrl(String shortUrl);

    boolean existsByShortUrl(String shortUrl);

    List<Url> findAllByUserId(Long userId);

    @Modifying
    @Query("""
           UPDATE Url u
           SET u.clickCount = u.clickCount + 1
           WHERE u.shortUrl = :shortUrl
           """)
    int incrementClickCount(@Param("shortUrl") String shortUrl);

    @Modifying
    @Query("""
           DELETE FROM Url u
           WHERE u.expiresAt IS NOT NULL
             AND u.expiresAt < :now
           """)
    int deleteExpiredUrls(@Param("now") LocalDateTime now);
}