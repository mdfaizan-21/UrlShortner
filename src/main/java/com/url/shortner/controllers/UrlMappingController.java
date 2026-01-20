package com.url.shortner.controllers;

import com.url.shortner.dtos.UrlMappingDTO;
import com.url.shortner.models.User;
import com.url.shortner.services.UrlMappingService;
import com.url.shortner.services.UserService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/urls")
@AllArgsConstructor
public class UrlMappingController {

    private UrlMappingService urlMappingService;
    private UserService userService;

    @PostMapping("/shorten")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<UrlMappingDTO> urlShortener(@RequestBody Map<String, String> map, Principal principal) {

        String originalUrl = map.get("originalUrl");
        User user = userService.getByUsername(principal.getName());
        UrlMappingDTO urlMappingDTO = urlMappingService.convertToShortUrl(originalUrl, user);
        return ResponseEntity.ok(urlMappingDTO);
    }

    @PostMapping("/myurls")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<UrlMappingDTO>> getAllUrls(Principal principal) {
        User user = userService.getByUsername(principal.getName());
        List<UrlMappingDTO> urls = urlMappingService.getAllUrlByUser(user);
        return ResponseEntity.ok(urls);
    }
}