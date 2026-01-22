package com.url.shortner.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * This class configures Cross-Origin Resource Sharing (CORS).
 * By default, browsers block scripts from one origin (e.g. frontend)
 * from making requests to a different origin (e.g. backend) for security.
 */
@Configuration // Marks this class as a source of bean definitions for the application context.
public class WebConfig implements WebMvcConfigurer {

    @Value("${frontend.url}")
    String frontEndUrl;

    /**
     * This method defines the specific rules for CORS.
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // 1. registry.addMapping("/**")
        // This applies these CORS rules to EVERY endpoint in your application.
        registry.addMapping("/**")

                // 2. .allowedOrigins(frontEndUrl)
                // Tells the browser: "I trust requests coming from this specific URL."
                // When allowCredentials is true, this MUST be a specific URL, not "*".
                .allowedOrigins(frontEndUrl)

                // 3. .allowedMethods(...)
                // Specifies which HTTP verbs are permitted from the cross-origin request.
                // "OPTIONS" is crucial because browsers send a "Preflight" request to check permissions.
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")

                // 4. .allowedHeaders("*")
                // Allows any HTTP headers to be sent in the request (e.g., Content-Type, Authorization).
                .allowedHeaders("*")

                // 5. .allowCredentials(true)
                // This is vital if frontend needs to send Cookies or Authorization headers (like JWTs)
                // back and forth with the server.
                .allowCredentials(true);
    }
}