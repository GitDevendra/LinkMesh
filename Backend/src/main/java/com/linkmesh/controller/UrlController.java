package com.linkmesh.controller;

import com.linkmesh.dto.UrlDto;
import com.linkmesh.dto.UserUrlResponse;
import com.linkmesh.service.UrlService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/v1/url")
@RequiredArgsConstructor
public class UrlController {

    private final UrlService urlService;

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("LinkMesh is up and running!");
    }

    @PostMapping
    public ResponseEntity<UrlDto.CreateResponse> createShortUrl( @Valid @RequestBody UrlDto.CreateRequest request) {
        log.info("POST /v1/url — {}", request.longUrl());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(urlService.createShortUrl(request));
    }

    @GetMapping("/{shortUrl}")
    public ResponseEntity<Void> redirect(
            @PathVariable String shortUrl) {

        String longUrl = urlService.getLongUrl(shortUrl);

        urlService.incrementClickCount(shortUrl);

        return ResponseEntity.status(HttpStatus.FOUND)
                .header(HttpHeaders.LOCATION, longUrl)
                .build();
    }

    @DeleteMapping("/{shortUrl}")
    public ResponseEntity<Void> deleteUrl(
            @PathVariable String shortUrl) {

        urlService.deleteUrl(shortUrl);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/my-urls")
    public ResponseEntity<List<UserUrlResponse>> getMyUrls() {
        return ResponseEntity.ok(urlService.getMyUrls());
    }

}