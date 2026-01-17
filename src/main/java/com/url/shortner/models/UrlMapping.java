package com.url.shortner.models;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
public class UrlMapping {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // Unique ID for this URL mapping
    private String originalUrl; // The long URL provided by the user
    private String shortUrl; // The generated short URL
    private int clickCount = 0; // Number of times the short URL has been clicked
    private LocalDateTime createdDate; // When this mapping was created

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user; // The user who created this mapping

    @OneToMany(mappedBy = "urlMapping")
    private List<ClickEvent> clickEvents; // List of click events for this mapping
}