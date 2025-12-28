package com.quadrah.sims.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.quadrah.sims.model.Holiday;

import java.time.LocalDate;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class HolidayDTO {
    private Long id;
    private String name;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate date;

    // Constructors
    public HolidayDTO() {}

    public HolidayDTO(String name, LocalDate date) {
        this.name = name;
        this.date = date;
    }

    public HolidayDTO(Holiday holiday) {
        this.id = holiday.getId();
        this.name = holiday.getName();
        this.date = holiday.getDate();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    @Override
    public String toString() {
        return "HolidayDTO{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", date=" + date +
                '}';
    }
}