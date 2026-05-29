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
public class HolidayDto {

    private Long id;
    private String code;
    private String name;
    private LocalDate fromDate;
    private LocalDate toDate;
    private String type;
}
