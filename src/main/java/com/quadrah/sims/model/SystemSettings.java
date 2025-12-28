package com.quadrah.sims.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "system_settings")
public class SystemSettings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "school_name")
    private String schoolName;

    @Column(name = "term_start")
    private LocalDate termStart;

    @Column(name = "term_end")
    private LocalDate termEnd;

    @Embedded
    private AlertParameters alertParameters = new AlertParameters();

    @OneToMany(mappedBy = "settings", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Holiday> holidays = new ArrayList<>();

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getSchoolName() { return schoolName; }
    public void setSchoolName(String schoolName) { this.schoolName = schoolName; }
    public LocalDate getTermStart() { return termStart; }
    public void setTermStart(LocalDate termStart) { this.termStart = termStart; }
    public LocalDate getTermEnd() { return termEnd; }
    public void setTermEnd(LocalDate termEnd) { this.termEnd = termEnd; }
    public AlertParameters getAlertParameters() { return alertParameters; }
    public void setAlertParameters(AlertParameters alertParameters) { this.alertParameters = alertParameters; }
    public List<Holiday> getHolidays() { return holidays; }
    public void setHolidays(List<Holiday> holidays) { this.holidays = holidays; }

    @Embeddable
    public static class AlertParameters {
        @Column(name = "low_stock_threshold")
        private Integer lowStock = 10;

        @Column(name = "expiry_days_threshold")
        private Integer expiryDays = 30;

        @Column(name = "visit_reminder_days")
        private Integer visitReminder = 7;

        // Getters and Setters
        public Integer getLowStock() { return lowStock; }
        public void setLowStock(Integer lowStock) { this.lowStock = lowStock; }
        public Integer getExpiryDays() { return expiryDays; }
        public void setExpiryDays(Integer expiryDays) { this.expiryDays = expiryDays; }
        public Integer getVisitReminder() { return visitReminder; }
        public void setVisitReminder(Integer visitReminder) { this.visitReminder = visitReminder; }
    }
}