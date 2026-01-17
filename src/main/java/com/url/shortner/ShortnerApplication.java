package com.url.shortner;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ShortnerApplication {

	// The main method is the entry point of the Java application.
	// When you run the application, execution starts here.
	public static void main(String[] args) {
		// SpringApplication.run() launches the Spring Boot application.
		// It sets up the default configuration, starts the embedded server (like
		// Tomcat),
		// and scans for components in the package.
		SpringApplication.run(ShortnerApplication.class, args);
	}

}
