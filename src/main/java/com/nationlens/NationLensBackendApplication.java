package com.nationlens;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class NationLensBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(NationLensBackendApplication.class, args);
	}

}
