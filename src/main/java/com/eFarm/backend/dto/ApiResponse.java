package com.eFarm.backend.dto;

import java.time.LocalDateTime;

public class ApiResponse<T> {

    private boolean success;
    private String message;
    private T data;
    private LocalDateTime timestamp;
    private int statusCode;
    private String error;

    // Constructors
    public ApiResponse() {
        this.timestamp = LocalDateTime.now();
    }

    public ApiResponse(boolean success, String message, T data) {
        this.success = success;
        this.message = message;
        this.data = data;
        this.timestamp = LocalDateTime.now();
    }

    public ApiResponse(boolean success, String message, T data, int statusCode) {
        this.success = success;
        this.message = message;
        this.data = data;
        this.statusCode = statusCode;
        this.timestamp = LocalDateTime.now();
    }

    // Static factory methods for success responses
    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(true, message, data, 200);
    }

    public static <T> ApiResponse<T> success(String message) {
        return new ApiResponse<>(true, message, null, 200);
    }

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, "Success", data, 200);
    }

    public static <T> ApiResponse<T> created(String message, T data) {
        return new ApiResponse<>(true, message, data, 201);
    }

    // Static factory methods for error responses
    public static <T> ApiResponse<T> error(String message) {
        ApiResponse<T> response = new ApiResponse<>(false, message, null, 400);
        response.setError(message);
        return response;
    }

    public static <T> ApiResponse<T> error(String message, int statusCode) {
        ApiResponse<T> response = new ApiResponse<>(false, message, null, statusCode);
        response.setError(message);
        return response;
    }

    public static <T> ApiResponse<T> badRequest(String message) {
        ApiResponse<T> response = new ApiResponse<>(false, message, null, 400);
        response.setError(message);
        return response;
    }

    public static <T> ApiResponse<T> unauthorized(String message) {
        ApiResponse<T> response = new ApiResponse<>(false, message, null, 401);
        response.setError(message);
        return response;
    }

    public static <T> ApiResponse<T> forbidden(String message) {
        ApiResponse<T> response = new ApiResponse<>(false, message, null, 403);
        response.setError(message);
        return response;
    }

    public static <T> ApiResponse<T> notFound(String message) {
        ApiResponse<T> response = new ApiResponse<>(false, message, null, 404);
        response.setError(message);
        return response;
    }

    public static <T> ApiResponse<T> conflict(String message) {
        ApiResponse<T> response = new ApiResponse<>(false, message, null, 409);
        response.setError(message);
        return response;
    }

    public static <T> ApiResponse<T> internalServerError(String message) {
        ApiResponse<T> response = new ApiResponse<>(false, message, null, 500);
        response.setError(message);
        return response;
    }

    // Getters and Setters
    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    @Override
    public String toString() {
        return "ApiResponse{" +
                "success=" + success +
                ", message='" + message + '\'' +
                ", data=" + data +
                ", timestamp=" + timestamp +
                ", statusCode=" + statusCode +
                ", error='" + error + '\'' +
                '}';
    }
}