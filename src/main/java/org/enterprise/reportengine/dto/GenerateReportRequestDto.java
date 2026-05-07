package org.enterprise.reportengine.dto;

import lombok.*;

import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GenerateReportRequestDto {

    private Map<String, Object> parameters;
}

