package org.enterprise.reportengine.dto;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.*;
import org.enterprise.reportengine.enums.ReportParamType;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportParameterDto {

    private String paramName;
    private String title;
    @Enumerated(EnumType.STRING)
    private ReportParamType paramType;
    private Boolean mandatory;
    private String dependedElement;
    private String defaultValue;
    private Boolean multiple;
    private String placeholder;
}
