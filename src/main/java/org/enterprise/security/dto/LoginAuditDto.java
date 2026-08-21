package org.enterprise.security.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class LoginAuditDto {
    private Long id;
    private String username;
    private String ipAddress;
    private String deviceInfo;
    private Boolean success;
    private LocalDateTime loginAt;
    private String failureReason;
}
