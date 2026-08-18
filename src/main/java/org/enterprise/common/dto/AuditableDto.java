package org.enterprise.common.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public abstract class AuditableDto {
    private Long id;
    private Long companyId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
