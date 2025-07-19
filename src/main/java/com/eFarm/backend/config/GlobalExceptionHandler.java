package com.eFarm.backend.config;

import com.eFarm.backend.dto.VerificationResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handle validation errors
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();

        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        System.err.println("❌ Gabime validimi: " + errors);

        // Merr gabimin e parë për ta shfaqur
        String firstError = errors.values().iterator().next();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(VerificationResponse.error("Gabim validimi: " + firstError));
    }

    /**
     * Handle runtime exceptions
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<?> handleRuntimeException(RuntimeException ex, WebRequest request) {
        System.err.println("❌ Runtime Exception: " + ex.getMessage());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(VerificationResponse.error(ex.getMessage()));
    }

    /**
     * Handle illegal argument exceptions
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> handleIllegalArgumentException(IllegalArgumentException ex) {
        System.err.println("❌ Illegal Argument: " + ex.getMessage());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(VerificationResponse.error("Parametër i pavlefshëm: " + ex.getMessage()));
    }

    /**
     * Handle all other exceptions
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleGenericException(Exception ex, WebRequest request) {
        // Injoro gabimet e favicon.ico
        if (ex.getMessage() != null && ex.getMessage().contains("favicon.ico")) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        System.err.println("❌ Generic Exception: " + ex.getMessage());
        ex.printStackTrace();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(VerificationResponse.error("Gabim i brendshëm i serverit. Provoni sërish."));
    }

    /**
     * Custom error response class
     */
    public static class ErrorResponse {
        private String timestamp;
        private int status;
        private String error;
        private String message;
        private String path;

        public ErrorResponse(int status, String error, String message, String path) {
            this.timestamp = LocalDateTime.now().toString();
            this.status = status;
            this.error = error;
            this.message = message;
            this.path = path;
        }

        // Getters
        public String getTimestamp() { return timestamp; }
        public int getStatus() { return status; }
        public String getError() { return error; }
        public String getMessage() { return message; }
        public String getPath() { return path; }
    }
}