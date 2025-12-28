package com.quadrah.sims.dto;

public class TrendDataDTO {
    private String period; // Could be "Jan", "Feb", or "2024-01", etc.
    private Integer visits;
    private Integer incidents;
    private Integer medicationCount;

    // Constructors, getters, setters
    public TrendDataDTO() {}

    public TrendDataDTO(String period, Integer visits, Integer incidents, Integer medicationCount) {
        this.period = period;
        this.visits = visits;
        this.incidents = incidents;
        this.medicationCount = medicationCount;
    }

    // Getters and setters
    public String getPeriod() { return period; }
    public void setPeriod(String period) { this.period = period; }

    public Integer getVisits() { return visits; }
    public void setVisits(Integer visits) { this.visits = visits; }

    public Integer getIncidents() { return incidents; }
    public void setIncidents(Integer incidents) { this.incidents = incidents; }

    public Integer getMedicationCount() { return medicationCount; }
    public void setMedicationCount(Integer medicationCount) { this.medicationCount = medicationCount; }
}
