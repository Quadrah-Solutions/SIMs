package com.quadrah.sims.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class BulkUploadError {
    private int rowNumber;
    private String studentId;
    private String field;
    private String error;

    public BulkUploadError() {}

    public BulkUploadError(int rowNumber, String studentId, String field, String error) {
        this.rowNumber = rowNumber;
        this.studentId = studentId;
        this.field = field;
        this.error = error;
    }

    // Getters and setters
    public int getRowNumber() { return rowNumber; }
    public void setRowNumber(int rowNumber) { this.rowNumber = rowNumber; }

    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }

    public String getField() { return field; }
    public void setField(String field) { this.field = field; }

    public String getError() { return error; }
    public void setError(String error) { this.error = error; }
}