package org.enterprise.reportengine.service;

import lombok.RequiredArgsConstructor;
import org.enterprise.reportengine.dto.ReportListDto;
import org.enterprise.reportengine.dto.ReportParameterDto;
import org.enterprise.reportengine.entity.AclReportDetail;
import org.enterprise.reportengine.entity.AclReportMaster;
import org.enterprise.reportengine.repository.AclReportDetailRepository;
import org.enterprise.reportengine.repository.AclReportMasterRepository;
import org.enterprise.reportengine.util.SqlSecurityValidator;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final AclReportMasterRepository reportMasterRepository;
    private final AclReportDetailRepository reportDetailRepository;
    private final NamedParameterJdbcTemplate jdbcTemplate;

    public List<String> getReportGroups() {

        return reportMasterRepository.findAll()
                .stream()
                .filter(r -> Boolean.TRUE.equals(r.getIsActive()))
                .map(AclReportMaster::getRptGroup)
                .distinct()
                .sorted()
                .toList();
    }

    public List<ReportListDto> getReportsByGroup(String group) {

        return reportMasterRepository
                .findByRptGroupAndIsActiveOrderBySortBy(group, true)
                .stream()
                .map(r -> ReportListDto.builder()
                        .code(r.getCode())
                        .title(r.getTitle())
                        .build())
                .collect(Collectors.toList());
    }

    public List<ReportParameterDto> getParameters(String reportCode) {

        return reportDetailRepository
                .findByAclReportMaster_CodeAndIsActiveOrderBySortBy(
                        reportCode,
                        true
                )
                .stream()
                .map(p -> ReportParameterDto.builder()
                        .paramName(p.getParamName())
                        .title(p.getTitle())
                        .paramType(p.getParamType())
                        .mandatory(p.getIsMandatory())
                        .dependedElement(p.getDependedElement())
                        .defaultValue(p.getDefaultValue())
                        .multiple(p.getMultiple())
                        .placeholder(p.getPlaceholder())
                        .build())
                .collect(Collectors.toList());
    }

    public List<Map<String, Object>> getDropdownData(
            String reportCode,
            String paramName,
            Map<String, Object> currentValues
    ) {

        AclReportDetail detail = reportDetailRepository
                .findByAclReportMaster_CodeAndParamNameAndIsActive(
                        reportCode,
                        paramName,
                        true
                )
                .orElseThrow(() -> new RuntimeException("Parameter not found"));

        SqlSecurityValidator.validateSelectQuery(detail.getQueryList());

        MapSqlParameterSource params = new MapSqlParameterSource();

        if (currentValues != null) {
            currentValues.forEach(params::addValue);
        }

        return jdbcTemplate.queryForList(
                detail.getQueryList(),
                params
        );
    }

    public List<Map<String, Object>> generateReport(
            String reportCode,
            Map<String, Object> requestParams
    ) {

        AclReportMaster report = reportMasterRepository.findByCodeAndIsActive(reportCode, true)
                .orElseThrow(() -> new RuntimeException("Report not found"));

        validateMandatoryParameters(reportCode, requestParams);

        SqlSecurityValidator.validateSelectQuery(
                report.getSqlQuery()
        );

        MapSqlParameterSource params = new MapSqlParameterSource();

        if (requestParams != null) {
            requestParams.forEach(params::addValue);
        }

        return jdbcTemplate.queryForList(
                report.getSqlQuery(),
                params
        );
    }

    private void validateMandatoryParameters(
            String reportCode,
            Map<String, Object> params
    ) {

        List<AclReportDetail> parameterList =
                reportDetailRepository
                        .findByAclReportMaster_CodeAndIsActiveOrderBySortBy(
                                reportCode,
                                true
                        );

        for (AclReportDetail detail : parameterList) {

            if (Boolean.TRUE.equals(detail.getIsMandatory())) {

                Object value = params.get(detail.getParamName());

                if (value == null || value.toString().isBlank()) {
                    throw new RuntimeException(
                            detail.getTitle() + " is mandatory"
                    );
                }
            }
        }
    }
}
