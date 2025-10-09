package com.quadrah.sims;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@OpenAPIDefinition(
	info = @Info(
			title = "Syte Infirmary Management System API",
			version = "1.0.0",
			description = "REST API for managing school infirmary operations"
	)
)
public class SIMsApplication {
	public static void main(String[] args) {
		SpringApplication.run(SIMsApplication.class, args);
	}

}
