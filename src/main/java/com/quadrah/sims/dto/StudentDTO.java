package com.quadrah.sims.dto;

import com.quadrah.sims.model.Allergy;
import com.quadrah.sims.model.Student;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public class StudentDTO {
    private Long id;
    private String studentId;
    private String firstName;
    private String lastName;
    private String fullName;
    private String gradeLevel;
    private String homeroom;
    private LocalDate dateOfBirth;
    private String gender;
    private String allergies;

    // Constructor
    public StudentDTO(Student student) {
        this.id = student.getId();
        this.studentId = student.getStudentId();
        this.firstName = student.getFirstName();
        this.lastName = student.getLastName();
        this.fullName = student.getFirstName() + " " + student.getLastName();
        this.gradeLevel = student.getGradeLevel();
        this.homeroom = student.getHomeroom();
        this.dateOfBirth = student.getDateOfBirth();
        this.gender = student.getGender();

        // Format allergies
        if (student.getAllergies() != null && !student.getAllergies().isEmpty()) {
            this.allergies = student.getAllergies().stream()
                    .map(Allergy::getAllergyType)
                    .collect(Collectors.joining(", "));
        } else {
            this.allergies = "None";
        }
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getGradeLevel() { return gradeLevel; }
    public void setGradeLevel(String gradeLevel) { this.gradeLevel = gradeLevel; }

    public String getHomeroom() { return homeroom; }
    public void setHomeroom(String homeroom) { this.homeroom = homeroom; }

    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(LocalDate dateOfBirth) { this.dateOfBirth = dateOfBirth; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public String getAllergies() { return allergies; }
    public void setAllergies(String allergies) { this.allergies = allergies; }
}
