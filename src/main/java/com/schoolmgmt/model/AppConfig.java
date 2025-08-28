package com.schoolmgmt.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "app_config")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "school_id")
    private String schoolId; // null for global config

    @Column(nullable = false)
    private String scope; // e.g., "features", "ui", "runtime"

    @Column(nullable = false)
    private String key; // e.g., "google_pay"

    @Column(columnDefinition = "jsonb", nullable = false)
    private String value; // stored as JSON string

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();
}
