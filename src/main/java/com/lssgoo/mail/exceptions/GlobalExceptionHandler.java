package com.lssgoo.mail.exceptions;

import com.lssgoo.mail.dtos.APIResponse;
import com.lssgoo.mail.dtos.Error;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<APIResponse<Object>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        List<Error> errors = new ArrayList<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.add(Error.builder()
                    .code("VALIDATION_ERROR")
                    .message(fieldName + ": " + errorMessage)
                    .timestamp(LocalDateTime.now())
                    .build());
        });

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(APIResponse.builder()
                        .success(false)
                        .message("Validation failed")
                        .errors(errors)
                        .timestamp(LocalDateTime.now())
                        .build());
    }

    @ExceptionHandler({BadCredentialsException.class, AuthenticationException.class})
    public ResponseEntity<APIResponse<Object>> handleAuthenticationException(Exception ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(APIResponse.builder()
                        .success(false)
                        .message("Authentication failed: " + ex.getMessage())
                        .timestamp(LocalDateTime.now())
                        .build());
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<APIResponse<Object>> handleRuntimeException(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(APIResponse.builder()
                        .success(false)
                        .message(ex.getMessage())
                        .timestamp(LocalDateTime.now())
                        .build());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<APIResponse<Object>> handleGenericException(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(APIResponse.builder()
                        .success(false)
                        .message("An unexpected error occurred: " + ex.getMessage())
                        .timestamp(LocalDateTime.now())
                        .build());
    }
}

