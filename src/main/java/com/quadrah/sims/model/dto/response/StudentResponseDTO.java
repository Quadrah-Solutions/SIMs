package com.quadrah.sims.model.dto.response;

import com.quadrah.sims.model.Gender;
import com.quadrah.sims.model.dto.request.EmergencyContactDTO;

import java.time.LocalDate;
import java.util.List;

public record StudentResponseDTO(
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