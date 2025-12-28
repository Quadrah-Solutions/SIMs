package com.quadrah.sims.service;

import com.quadrah.sims.dto.SettingsDTO;
import com.quadrah.sims.dto.HolidayDTO;
import com.quadrah.sims.model.SystemSettings;
import com.quadrah.sims.model.Holiday;
import com.quadrah.sims.repository.SettingsRepository;
import com.quadrah.sims.repository.HolidayRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.List;

@Service
public class SettingsService {

    private final SettingsRepository settingsRepository;
    private final HolidayRepository holidayRepository;

    public SettingsService(SettingsRepository settingsRepository, HolidayRepository holidayRepository) {
        this.settingsRepository = settingsRepository;
        this.holidayRepository = holidayRepository;
    }

    public SettingsDTO getSettings() {
        List<SystemSettings> settingsList = settingsRepository.findAll();

        if (settingsList.isEmpty()) {
            // Create default settings if none exist
            return createDefaultSettings();
        }

        // Return the first (and should be only) settings record
        return new SettingsDTO(settingsList.get(0));
    }

    private SettingsDTO createDefaultSettings() {
        SystemSettings defaultSettings = new SystemSettings();
        defaultSettings.setSchoolName("Default School");

        // Initialize alert parameters
        SystemSettings.AlertParameters alertParams = new SystemSettings.AlertParameters();
        alertParams.setLowStock(10);
        alertParams.setExpiryDays(30);
        alertParams.setVisitReminder(7);
        defaultSettings.setAlertParameters(alertParams);

        SystemSettings savedSettings = settingsRepository.save(defaultSettings);
        return new SettingsDTO(savedSettings);
    }

    @Transactional
    public SettingsDTO saveSettings(SettingsDTO settingsDTO) {
        List<SystemSettings> settingsList = settingsRepository.findAll();
        SystemSettings settings;

        if (settingsList.isEmpty()) {
            settings = new SystemSettings();
        } else {
            settings = settingsList.get(0);
        }

        // Update basic settings
        settings.setSchoolName(settingsDTO.getSchoolName());
        settings.setTermStart(settingsDTO.getTermStart());
        settings.setTermEnd(settingsDTO.getTermEnd());

        // Update alert parameters
        if (settingsDTO.getAlertParameters() != null) {
            if (settings.getAlertParameters() == null) {
                settings.setAlertParameters(new SystemSettings.AlertParameters());
            }
            settings.getAlertParameters().setLowStock(settingsDTO.getAlertParameters().getLowStock());
            settings.getAlertParameters().setExpiryDays(settingsDTO.getAlertParameters().getExpiryDays());
            settings.getAlertParameters().setVisitReminder(settingsDTO.getAlertParameters().getVisitReminder());
        }

        SystemSettings savedSettings = settingsRepository.save(settings);
        return new SettingsDTO(savedSettings);
    }

    @Transactional
    public SettingsDTO addHoliday(HolidayDTO holidayDTO) {
        List<SystemSettings> settingsList = settingsRepository.findAll();

        if (settingsList.isEmpty()) {
            throw new RuntimeException("Settings not found. Please create settings first.");
        }

        SystemSettings settings = settingsList.get(0);

        Holiday holiday = new Holiday();
        holiday.setName(holidayDTO.getName());
        holiday.setDate(holidayDTO.getDate());
        holiday.setSettings(settings);

        Holiday savedHoliday = holidayRepository.save(holiday);
        settings.getHolidays().add(savedHoliday);

        SystemSettings savedSettings = settingsRepository.save(settings);
        return new SettingsDTO(savedSettings);
    }

    @Transactional
    public SettingsDTO deleteHoliday(Long holidayId) {
        Optional<Holiday> holidayOptional = holidayRepository.findById(holidayId);

        if (holidayOptional.isEmpty()) {
            throw new RuntimeException("Holiday not found with id: " + holidayId);
        }

        Holiday holiday = holidayOptional.get();
        SystemSettings settings = holiday.getSettings();

        settings.getHolidays().remove(holiday);
        holidayRepository.delete(holiday);

        SystemSettings savedSettings = settingsRepository.save(settings);
        return new SettingsDTO(savedSettings);
    }
}