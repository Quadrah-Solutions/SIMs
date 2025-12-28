package com.quadrah.sims.dto;

public class VisitSummaryDTO {
    private String visitType;
    private Long count;
    private String change; // e.g., "+12%", "-5%"

    // Constructors, getters, setters
    public VisitSummaryDTO() {}

    public VisitSummaryDTO(String visitType, Long count, String change) {
        this.visitType = visitType;
        this.count = count;
        this.change = change;
    }

    // Getters and setters
    public String getVisitType() { return visitType; }
    public void setVisitType(String visitType) { this.visitType = visitType; }

    public Long getCount() { return count; }
    public void setCount(Long count) { this.count = count; }

    public String getChange() { return change; }
    public void setChange(String change) { this.change = change; }
}
