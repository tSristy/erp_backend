package org.enterprise.common.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

@Entity
@Table(name = "system_audit_logs")
@Getter
@Setter
public class SystemAuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String entityName;

    @Column(nullable = false)
    private String entityId;

    @Column(nullable = false)
    private String action; // INSERT, UPDATE, DELETE

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private String oldState;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private String newState;

    private String changedBy;

    @Column(nullable = false)
    private LocalDateTime changedAt;
}
