package com.quadrah.sims.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quadrah.sims.dto.SettingsDTO;
import com.quadrah.sims.dto.HolidayDTO;
import com.quadrah.sims.service.SettingsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.server.ResponseStatusException;

import java.io.BufferedReader;

@RestController
@RequestMapping("/api/settings")
@Tag(name = "Settings Management", description = "APIs for managing system settings")
@SecurityRequirement(name = "bearerAuth")
public class SettingsController {

    private static final Logger log = LoggerFactory.getLogger(SettingsController.class);
    private final SettingsService settingsService;
    private final ObjectMapper objectMapper;

    public SettingsController(SettingsService settingsService, ObjectMapper objectMapper) {
        this.settingsService = settingsService;
        this.objectMapper = objectMapper;
    }

    @Operation(
            summary = "Get system settings",
            description = "Retrieve current system settings including term dates, alert parameters, and holidays"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully retrieved settings"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - valid JWT token required")
    })
    @GetMapping
    public ResponseEntity<SettingsDTO> getSettings() {
        SettingsDTO settings = settingsService.getSettings();
        return ResponseEntity.ok(settings);
    }

    @Operation(
            summary = "Save system settings",
            description = "Update system settings including term dates and alert parameters"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully saved settings"),
            @ApiResponse(responseCode = "400", description = "Invalid settings data"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - valid JWT token required")
    })
    @PostMapping
    public ResponseEntity<SettingsDTO> saveSettings(@RequestBody SettingsDTO settingsDTO) {
        log.info("Received settings DTO: {}", settingsDTO);
        SettingsDTO savedSettings = settingsService.saveSettings(settingsDTO);
        return ResponseEntity.ok(savedSettings);
    }

    @Operation(
            summary = "Add a holiday",
            description = "Add a new holiday to the system settings"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully added holiday"),
            @ApiResponse(responseCode = "400", description = "Invalid holiday data"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - valid JWT token required"),
            @ApiResponse(responseCode = "404", description = "Settings not found")
    })
    @PostMapping("/holidays")
    public ResponseEntity<SettingsDTO> addHoliday(@RequestBody HolidayDTO holidayDTO) {
        try {
            SettingsDTO settings = settingsService.addHoliday(holidayDTO);
            return ResponseEntity.ok(settings);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }

    @Operation(
            summary = "Delete a holiday",
            description = "Remove a holiday from the system settings"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully deleted holiday"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - valid JWT token required"),
            @ApiResponse(responseCode = "404", description = "Holiday not found")
    })
    @DeleteMapping("/holidays/{id}")
    public ResponseEntity<SettingsDTO> deleteHoliday(@PathVariable Long id) {
        SettingsDTO updatedSettings = settingsService.deleteHoliday(id);
        return ResponseEntity.ok(updatedSettings);
    }
}