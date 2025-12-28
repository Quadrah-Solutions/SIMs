package com.quadrah.sims.repository;

import com.quadrah.sims.model.SystemSettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SettingsRepository extends JpaRepository<SystemSettings, Long> {
}