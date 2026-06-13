package com.linkmesh.exception;

import com.linkmesh.dto.UrlDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UrlNotFoundException.class)
    public ResponseEntity<UrlDto.ErrorResponse> handleNotFound(UrlNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new UrlDto.ErrorResponse(404, "Not Found", ex.getMessage()));
    }

    @ExceptionHandler(UrlExpiredException.class)
    public ResponseEntity<UrlDto.ErrorResponse> handleExpired(UrlExpiredException ex) {
        return ResponseEntity.status(HttpStatus.GONE)
                .body(new UrlDto.ErrorResponse(410, "Gone", ex.getMessage()));
    }

    @ExceptionHandler(AliasAlreadyExistsException.class)
    public ResponseEntity<UrlDto.ErrorResponse> handleConflict(AliasAlreadyExistsException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new UrlDto.ErrorResponse(409, "Conflict", ex.getMessage()));
    }

    @ExceptionHandler(PremiumRequiredException.class)
    public ResponseEntity<UrlDto.ErrorResponse> handlePremiumRequired(PremiumRequiredException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new UrlDto.ErrorResponse(403, "Premium Required", ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<UrlDto.ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        String details = ex.getBindingResult().getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new UrlDto.ErrorResponse(400, "Bad Request", details));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<UrlDto.ErrorResponse> handleGeneric(Exception ex) {
        log.error("Unhandled exception", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new UrlDto.ErrorResponse(500, "Internal Server Error",
                        "Something went wrong. Please try again later."));
    }
}