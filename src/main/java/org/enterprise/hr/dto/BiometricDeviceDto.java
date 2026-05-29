package org.enterprise.hr.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BiometricDeviceDto {

    private Long id;
    private String deviceCode;
    private String deviceName;
    private String ipAddress;
    private Integer port;
}
