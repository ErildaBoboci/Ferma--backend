package com.eFarm.backend.dto;

public class VerificationResponse {
    private boolean success;
    private String message;
    private String nextStep;

    public VerificationResponse() {}

    public VerificationResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public VerificationResponse(boolean success, String message, String nextStep) {
        this.success = success;
        this.message = message;
        this.nextStep = nextStep;
    }

    public static VerificationResponse success(String message) {
        return new VerificationResponse(true, message);
    }

    public static VerificationResponse success(String message, String nextStep) {
        return new VerificationResponse(true, message, nextStep);
    }

    public static VerificationResponse error(String message) {
        return new VerificationResponse(false, message);
    }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getNextStep() { return nextStep; }
    public void setNextStep(String nextStep) { this.nextStep = nextStep; }
}