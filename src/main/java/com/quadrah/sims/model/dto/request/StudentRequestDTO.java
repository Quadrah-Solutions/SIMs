package com.quadrah.sims.model.dto.request;

import com.quadrah.sims.model.Gender;
import java.time.LocalDate;
import java.util.List;

public record StudentRequestDTO(
        String studentId,
        String firstName,
        String lastName,
        String gradeLevel,
        String homeroom,
        LocalDate dateOfBirth,
        Gender gender,
        String specialNotes,
        List<EmergencyContactDTO> emergencyContacts
) {}
