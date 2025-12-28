package com.quadrah.sims.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.quadrah.sims.model.Holiday;
import com.quadrah.sims.model.SystemSettings;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class SettingsDTO {

    private Long id;
    private String schoolName;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate termStart;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate termEnd;

    private AlertParametersDTO alertParameters;
    private List<HolidayDTO> holidays;

    // Default constructor for Jackson
    public SettingsDTO() {}

    // Constructor from entity
    public SettingsDTO(SystemSettings settings) {
        this.id = settings.getId();
        this.schoolName = settings.getSchoolName();
        this.termStart = settings.getTermStart();
        this.termEnd = settings.getTermEnd();

        if (settings.getAlertParameters() != null) {
            this.alertParameters = new AlertParametersDTO(
                    settings.getAlertParameters().getLowStock(),
                    settings.getAlertParameters().getExpiryDays(),
                    settings.getAlertParameters().getVisitReminder()
            );
        } else {
            this.alertParameters = new AlertParametersDTO(10, 30, 7);
        }

        if (settings.getHolidays() != null) {
            this.holidays = settings.getHolidays().stream()
                    .map(HolidayDTO::new)
                    .collect(Collectors.toList());
        }
    }

    // Static inner class for alert parameters with proper annotations
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class AlertParametersDTO {
        private Integer lowStock;
        private Integer expiryDays;
        private Integer visitReminder;

        // Default constructor for Jackson
        public AlertParametersDTO() {}

        public AlertParametersDTO(Integer lowStock, Integer expiryDays, Integer visitReminder) {
            this.lowStock = lowStock;
            this.expiryDays = expiryDays;
            this.visitReminder = visitReminder;
        }

        // Getters and Setters
        public Integer getLowStock() { return lowStock; }
        public void setLowStock(Integer lowStock) { this.lowStock = lowStock; }
        public Integer getExpiryDays() { return expiryDays; }
        public void setExpiryDays(Integer expiryDays) { this.expiryDays = expiryDays; }
        public Integer getVisitReminder() { return visitReminder; }
        public void setVisitReminder(Integer visitReminder) { this.visitReminder = visitReminder; }
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getSchoolName() { return schoolName; }
    public void setSchoolName(String schoolName) { this.schoolName = schoolName; }
    public LocalDate getTermStart() { return termStart; }
    public void setTermStart(LocalDate termStart) { this.termStart = termStart; }
    public LocalDate getTermEnd() { return termEnd; }
    public void setTermEnd(LocalDate termEnd) { this.termEnd = termEnd; }
    public AlertParametersDTO getAlertParameters() { return alertParameters; }
    public void setAlertParameters(AlertParametersDTO alertParameters) { this.alertParameters = alertParameters; }
    public List<HolidayDTO> getHolidays() { return holidays; }
    public void setHolidays(List<HolidayDTO> holidays) { this.holidays = holidays; }

    @Override
    public String toString() {
        return "SettingsDTO{" +
                "id=" + id +
                ", schoolName='" + schoolName + '\'' +
                ", termStart=" + termStart +
                ", termEnd=" + termEnd +
                ", alertParameters=" + alertParameters +
                ", holidays=" + holidays +
                '}';
    }
}