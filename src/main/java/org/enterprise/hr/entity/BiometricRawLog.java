package org.enterprise.hr.entity;

import java.time.LocalDateTime;

import org.enterprise.common.entity.AuditableEntity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "hr_biometric_raw_log")
@Getter
@Setter
public class BiometricRawLog extends AuditableEntity {

    private String biometricId;

    private LocalDateTime logTime;

    private String punchType;

}
