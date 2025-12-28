package com.quadrah.sims.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.quadrah.sims.model.StudentVisit;

import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class VisitDTO {
    private Long id;
    private Long studentId;
    private String studentName;
    private Long nurseId;
    private String nurseName;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime visitDate;

    private String reason;
    private String symptoms;
    private String observations;
    private String vitalSigns;
    private StudentVisit.DispositionType disposition;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime dispositionTime;

    private String finalAssessment;
    private Boolean emergencyFlag;
    private String referredBy;

    private String grade;
    private String className;

    // Constructor from entity
    public VisitDTO(StudentVisit visit) {
        this.id = visit.getId();
        this.studentId = visit.getStudent() != null ? visit.getStudent().getId() : null;
        this.studentName = visit.getStudent() != null ?
                visit.getStudent().getFirstName() + " " + visit.getStudent().getLastName() : null;
        this.nurseId = visit.getNurse() != null ? visit.getNurse().getId() : null;
        this.nurseName = visit.getNurse() != null ?
                visit.getNurse().getFirstName() + " " + visit.getNurse().getLastName() : null;
        this.visitDate = visit.getVisitDate();
        this.reason = visit.getReason();
        this.symptoms = visit.getSymptoms();
        this.observations = visit.getObservations();
        this.vitalSigns = visit.getVitalSigns();
        this.disposition = visit.getDisposition();
        this.dispositionTime = visit.getDispositionTime();
        this.finalAssessment = visit.getFinalAssessment();
        this.emergencyFlag = visit.getEmergencyFlag();
        this.referredBy = visit.getReferredBy();

        if (visit.getStudent() != null) {
            // Use gradeLevel (string field) or get from grade entity
            if (visit.getStudent().getGradeLevel() != null && !visit.getStudent().getGradeLevel().isEmpty()) {
                this.grade = visit.getStudent().getGradeLevel();
            } else if (visit.getStudent().getGrade() != null) {
                this.grade = visit.getStudent().getGrade().getGradeName();
            }

            // Get class name from classRoom entity or homeroom
            if (visit.getStudent() != null) {
                // Grade
                if (visit.getStudent().getGrade() != null) {
                    this.grade = visit.getStudent().getGrade().getGradeName();
                } else if (visit.getStudent().getGradeLevel() != null) {
                    this.grade = visit.getStudent().getGradeLevel();
                }

                // Class
                if (visit.getStudent().getClassRoom() != null) {
                    this.className = visit.getStudent().getClassRoom().getClassName();
                } else if (visit.getStudent().getHomeroom() != null) {
                    this.className = visit.getStudent().getHomeroom();
                }
            }
        }
    }

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }
    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }
    public Long getNurseId() { return nurseId; }
    public void setNurseId(Long nurseId) { this.nurseId = nurseId; }
    public String getNurseName() { return nurseName; }
    public void setNurseName(String nurseName) { this.nurseName = nurseName; }
    public LocalDateTime getVisitDate() { return visitDate; }
    public void setVisitDate(LocalDateTime visitDate) { this.visitDate = visitDate; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public String getSymptoms() { return symptoms; }
    public void setSymptoms(String symptoms) { this.symptoms = symptoms; }
    public String getObservations() { return observations; }
    public void setObservations(String observations) { this.observations = observations; }
    public String getVitalSigns() { return vitalSigns; }
    public void setVitalSigns(String vitalSigns) { this.vitalSigns = vitalSigns; }
    public StudentVisit.DispositionType getDisposition() { return disposition; }
    public void setDisposition(StudentVisit.DispositionType disposition) { this.disposition = disposition; }
    public LocalDateTime getDispositionTime() { return dispositionTime; }
    public void setDispositionTime(LocalDateTime dispositionTime) { this.dispositionTime = dispositionTime; }
    public String getFinalAssessment() { return finalAssessment; }
    public void setFinalAssessment(String finalAssessment) { this.finalAssessment = finalAssessment; }
    public Boolean getEmergencyFlag() { return emergencyFlag; }
    public void setEmergencyFlag(Boolean emergencyFlag) { this.emergencyFlag = emergencyFlag; }
    public String getReferredBy() { return referredBy; }
    public void setReferredBy(String referredBy) { this.referredBy = referredBy; }

    public String getGrade() { return grade; }
    public void setGrade(String grade) { this.grade = grade; }
    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }


}