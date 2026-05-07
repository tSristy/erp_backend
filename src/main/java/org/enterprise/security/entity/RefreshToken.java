package org.enterprise.security.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.enterprise.common.entity.AuditableEntity;

import java.time.LocalDateTime;

@Entity
@Table(name = "refresh_tokens")
@Getter
@Setter
public class RefreshToken extends AuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    private User user;

    @Column(unique = true, nullable = false)
    private String token;

    private LocalDateTime expiryDate;

    private Boolean revoked = false;
}