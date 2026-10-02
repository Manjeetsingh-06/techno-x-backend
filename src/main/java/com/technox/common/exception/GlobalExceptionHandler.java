package com.technox.common.exception;

import com.technox.common.dto.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Object>> handleBusinessException(BusinessException ex) {
        log.warn("Business exception: [{}] {}", ex.getErrorCode(), ex.getMessage());
        HttpStatus status = switch (ex.getErrorCode()) {
            case RESOURCE_NOT_FOUND, EVENT_NOT_FOUND, WAITLIST_NOT_FOUND -> HttpStatus.NOT_FOUND;
            case FORBIDDEN -> HttpStatus.FORBIDDEN;
            case UNAUTHORIZED, INVALID_CREDENTIALS -> HttpStatus.UNAUTHORIZED;
            case ALREADY_REGISTERED, STUDENT_ID_ALREADY_EXISTS, EMAIL_ALREADY_EXISTS -> HttpStatus.CONFLICT;
            default -> HttpStatus.BAD_REQUEST;
        };

        Map<String, Object> errData = Map.of("code", ex.getErrorCode().name());
        return ResponseEntity.status(status).body(
                ApiResponse.builder()
                        .success(false)
                        .message(ex.getMessage())
                        .data(errData)
                        .build()
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Object>> handleValidationException(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            errors.put(fieldError.getField(), fieldError.getDefaultMessage());
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                ApiResponse.builder()
                        .success(false)
                        .message("Validation failed")
                        .data(errors)
                        .build()
        );
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Object>> handleAccessDenied(AccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                ApiResponse.builder()
                        .success(false)
                        .message("Access denied: You do not have permission for this resource")
                        .data(Map.of("code", ErrorCode.FORBIDDEN.name()))
                        .build()
        );
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<Object>> handleAuthenticationException(AuthenticationException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                ApiResponse.builder()
                        .success(false)
                        .message("Authentication failed: " + ex.getMessage())
                        .data(Map.of("code", ErrorCode.UNAUTHORIZED.name()))
                        .build()
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleGenericException(Exception ex) {
        log.error("Unhandled server error: ", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                ApiResponse.builder()
                        .success(false)
                        .message("An unexpected internal error occurred. Please contact system support.")
                        .data(Map.of("code", ErrorCode.INTERNAL_SERVER_ERROR.name()))
                        .build()
        );
    }
}
