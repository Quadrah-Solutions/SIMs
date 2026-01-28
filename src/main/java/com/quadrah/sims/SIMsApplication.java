package com.quadrah.sims;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "com.quadrah.sims")
public class SIMsApplication {
	public static void main(String[] args) {
		SpringApplication.run(SIMsApplication.class, args);
	}
}