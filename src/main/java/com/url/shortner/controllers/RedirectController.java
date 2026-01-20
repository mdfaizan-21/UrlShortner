package com.url.shortner.controllers;

import com.url.shortner.models.UrlMapping;
import com.url.shortner.services.UrlMappingService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
@AllArgsConstructor
public class RedirectController {

    private UrlMappingService urlMappingService;

    @GetMapping("/{shortUrl}")
    public ResponseEntity<Void> redirect(@PathVariable String shortUrl) {
        // 1. Retrieve the mapping also
        //    increments the click count and saves a ClickEvent for analytics.
        UrlMapping urlMapping = urlMappingService.getOriginalUrl(shortUrl);

        if (urlMapping != null) {
            String originalUrl = urlMapping.getOriginalUrl();

            // 2. PROTOCOL VALIDATION:
            // Browsers treat "www.google.com" as a relative path on your server.
            // We check if it starts with a protocol; if not, we prepend "http://"
            // to make it an absolute URL.
            if (!originalUrl.startsWith("http://") && !originalUrl.startsWith("https://")) {
                originalUrl = "http://" + originalUrl;
            }

            // 3. HTTP 302 (FOUND):
            // We send a 302 status code and the "Location" header.
            // URI.create() converts the string into a valid URI object for the header.
            return ResponseEntity.status(302)
                    .location(URI.create(originalUrl))
                    .build();
        } else {
            // 4. ERROR HANDLING:
            // If the shortUrl doesn't exist in our DB, return a 404 response.
            return ResponseEntity.notFound().build();
        }
    }
}