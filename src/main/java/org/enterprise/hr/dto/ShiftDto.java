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
public class ShiftDto {

    private Long id;
    private String code;
    private String name;
    private LocalTime inTime;
    private LocalTime outTime;
    private Integer graceMinutes;
}
