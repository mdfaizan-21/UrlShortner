package com.url.shortner.dtos;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UrlMappingDTO {
        private String originalUrl;
        private String shortUrl;
        private int clickCount;
        private Long id;
        private LocalDateTime createdTime;
        private String username;
}
