package com.rmis.rmis;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import jakarta.annotation.PostConstruct;

@SpringBootApplication
public class RmisApplication {

	// @Value("${spring.servlet.multipart.enabled}")
	// private String multipartEnabled;

	@Value("${spring.servlet.multipart.max-file-size}")
	private String maxFileSize;

	@Value("${spring.servlet.multipart.max-request-size}")
	private String maxRequestSize;

	@Value("${server.tomcat.max-part-count}")
	private String tomcatCount;

	@Value("${spring.servlet.multipart.file-size-threshold}")
	private String threshold;

	public static void main(String[] args) {
		SpringApplication.run(RmisApplication.class, args);
	}

	@PostConstruct
    public void logEnvironmentVariables() {
        System.out.println("========== ENVIRONMENT VARIABLES ==========");
        
        // Log specific variables you care about
        // System.out.println("multipartEnabled: "+multipartEnabled);
        System.out.println("maxFileSize: " + maxFileSize);
        System.out.println("maxRequestSize: " + maxRequestSize);
        System.out.println("tomcatCount: " + tomcatCount);
        System.out.println("threshold: " + threshold);
        

        System.out.println("========== END ==========");
    }

}
