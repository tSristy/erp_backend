package org.enterprise.reportengine.controller;

import lombok.RequiredArgsConstructor;
import org.enterprise.reportengine.dto.DropdownRequestDto;
import org.enterprise.reportengine.dto.GenerateReportRequestDto;
import org.enterprise.reportengine.enums.ReportOutputFormat;
import org.enterprise.reportengine.service.ExcelExportService;
import org.enterprise.reportengine.service.PdfExportService;
import org.enterprise.reportengine.service.ReportService;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;
    private final ExcelExportService excelExportService;
    private final PdfExportService pdfExportService;

    @GetMapping("/groups")
    public ResponseEntity<List<String>> getGroups() {

        return ResponseEntity.ok(
                reportService.getReportGroups()
        );
    }

    @GetMapping("/by-group/{group}")
    public ResponseEntity<?> getReports(
            @PathVariable String group
    ) {

        return ResponseEntity.ok(
                reportService.getReportsByGroup(group)
        );
    }

    @GetMapping("/{reportCode}/parameters")
    public ResponseEntity<?> getParameters(
            @PathVariable String reportCode
    ) {

        return ResponseEntity.ok(
                reportService.getParameters(reportCode)
        );
    }

    @PostMapping("/dropdown-data")
    public ResponseEntity<?> getDropdownData(
            @RequestBody DropdownRequestDto request
    ) {

        return ResponseEntity.ok(
                reportService.getDropdownData(
                        request.getReportCode(),
                        request.getParamName(),
                        request.getCurrentValues()
                )
        );
    }

    @PostMapping("/{reportCode}/generate")
    public ResponseEntity<?> generateReport(
            @PathVariable String reportCode,
            @RequestParam ReportOutputFormat format,
            @RequestBody GenerateReportRequestDto request
    ) throws Exception {

        List<Map<String, Object>> data =
                reportService.generateReport(
                        reportCode,
                        request.getParameters()
                );

        if (format == ReportOutputFormat.HTML) {
            return ResponseEntity.ok(data);
        }

        if (format == ReportOutputFormat.XLSX) {

            ByteArrayInputStream excel =
                    excelExportService.export(data);

            return ResponseEntity.ok()
                    .header(
                            HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=report.xlsx"
                    )
                    .contentType(
                            MediaType.APPLICATION_OCTET_STREAM
                    )
                    .body(new InputStreamResource(excel));
        }

        ByteArrayInputStream pdf =
                pdfExportService.export(data);

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=report.pdf"
                )
                .contentType(MediaType.APPLICATION_PDF)
                .body(new InputStreamResource(pdf));
    }
}

