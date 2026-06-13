package com.linkmesh.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import java.time.LocalDateTime;

public final class UrlDto {

    private UrlDto() {}

    public record CreateRequest(

            @NotBlank(message = "longUrl must not be blank")
            @Pattern(
                    regexp = "^https?://.*",
                    message = "longUrl must start with http:// or https://"
            )
            String longUrl,

            @Pattern(
                    regexp = "^[a-zA-Z0-9]{4,16}$",
                    message = "customAlias must be 4–16 alphanumeric characters"
            )
            String customAlias,

            @Positive(message = "expiryDays must be a positive integer")
            Integer expiryDays

    ) {}

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record CreateResponse(
            String shortUrl,
            String longUrl,
            LocalDateTime createdAt,
            LocalDateTime expiresAt,
            String message
    ) {}

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record ErrorResponse(
            int status,
            String error,
            String message,
            LocalDateTime timestamp
    ) {
        public ErrorResponse(int status, String error, String message) {
            this(status, error, message, LocalDateTime.now());
        }
    }
}