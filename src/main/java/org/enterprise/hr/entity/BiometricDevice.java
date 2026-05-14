package org.enterprise.hr.entity;

import org.enterprise.common.entity.AuditableEntity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "hr_biometric_device")
@Getter
@Setter
public class BiometricDevice extends AuditableEntity {

    private String deviceCode;
    private String deviceName;
    private String ipAddress;
    private Integer port;

}
