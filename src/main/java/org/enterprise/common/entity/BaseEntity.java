package org.enterprise.common.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@MappedSuperclass
@Getter
@Setter
@com.fasterxml.jackson.annotation.JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public abstract class BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (updatedAt == null) {
            updatedAt = LocalDateTime.now();
        }
        onPrePersist();
    }

    protected void onPrePersist() {
        // Subclasses can hook into persist without duplicate JPA annotations
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
        onPreUpdate();
    }

    protected void onPreUpdate() {
        // Subclasses can hook into update without duplicate JPA annotations
    }
}