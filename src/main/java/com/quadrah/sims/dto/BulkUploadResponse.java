package com.quadrah.sims.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class BulkUploadResponse {
    private String message;
    private int successfulCount;
    private int failedCount;
    private List<BulkUploadError> errors;

    public BulkUploadResponse() {}

    public BulkUploadResponse(String message, int successfulCount, int failedCount, List<BulkUploadError> errors) {
        this.message = message;
        this.successfulCount = successfulCount;
        this.failedCount = failedCount;
        this.errors = errors;
    }

    // Getters and setters
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public int getSuccessfulCount() { return successfulCount; }
    public void setSuccessfulCount(int successfulCount) { this.successfulCount = successfulCount; }

    public int getFailedCount() { return failedCount; }
    public void setFailedCount(int failedCount) { this.failedCount = failedCount; }

    public List<BulkUploadError> getErrors() { return errors; }
    public void setErrors(List<BulkUploadError> errors) { this.errors = errors; }
}