package com.linkmesh.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.GONE)
public class UrlExpiredException extends RuntimeException {
    public UrlExpiredException(String shortUrl) {
        super("Short URL has expired: " + shortUrl);
    }
}