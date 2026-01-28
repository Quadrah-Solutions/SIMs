package com.quadrah.sims.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void configurePathMatch(PathMatchConfigurer configurer) {
        configurer.setUseTrailingSlashMatch(false);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // DISABLE static resource handling for /api/** paths
        // This prevents Spring from trying to serve /api/** as static resources
        registry.setOrder(Integer.MIN_VALUE);

        // Only enable static resources for specific paths if needed
        // For API-only app, you don't need static resources
    }
}