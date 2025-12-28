package com.quadrah.sims.dto;

import java.time.LocalDateTime;

public class ComplianceIssueDTO {
    private String issueType;
    private String studentName;
    private String grade;
    private LocalDateTime date;
    private String status;

    // Constructors, getters, setters
    public ComplianceIssueDTO() {}

    public ComplianceIssueDTO(String issueType, String studentName, String grade, LocalDateTime date, String status) {
        this.issueType = issueType;
        this.studentName = studentName;
        this.grade = grade;
        this.date = date;
        this.status = status;
    }

    // Getters and setters
    public String getIssueType() { return issueType; }
    public void setIssueType(String issueType) { this.issueType = issueType; }

    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }

    public String getGrade() { return grade; }
    public void setGrade(String grade) { this.grade = grade; }

    public LocalDateTime getDate() { return date; }
    public void setDate(LocalDateTime date) { this.date = date; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}