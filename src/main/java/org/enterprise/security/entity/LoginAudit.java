package org.enterprise.security.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.enterprise.common.entity.AuditableEntity;

import java.time.LocalDateTime;

@Entity
@Table(name = "login_audit_logs")
@Getter
@Setter
public class LoginAudit extends AuditableEntity {

    private String username;

    private String ipAddress;

    private String deviceInfo;

    private Boolean success;

    private LocalDateTime loginAt;

    private String failureReason;
}