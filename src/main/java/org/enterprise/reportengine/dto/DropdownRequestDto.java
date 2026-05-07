package org.enterprise.reportengine.dto;

import lombok.*;

import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DropdownRequestDto {

    private String reportCode;
    private String paramName;
    private Map<String, Object> currentValues;
}
