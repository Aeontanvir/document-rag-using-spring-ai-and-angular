package com.aeon.documentrag.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class DocumentRagBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(DocumentRagBackendApplication.class, args);
	}

}
