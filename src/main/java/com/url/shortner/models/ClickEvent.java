package com.url.shortner.models;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
public class ClickEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // Unique ID for each click event
    private LocalDateTime clickDate; // Date and time when the click occurred

    @ManyToOne
    @JoinColumn(name = "url_mapping_id")
    private UrlMapping urlMapping; // Link to the associated UrlMapping
}