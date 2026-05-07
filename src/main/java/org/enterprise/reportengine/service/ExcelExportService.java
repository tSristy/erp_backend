package org.enterprise.reportengine.service;

import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ExcelExportService {

    public ByteArrayInputStream export(
            List<Map<String, Object>> data
    ) throws Exception {

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Report");

        if (!data.isEmpty()) {

            List<String> columns =
                    new ArrayList<>(data.get(0).keySet());

            Row header = sheet.createRow(0);

            for (int i = 0; i < columns.size(); i++) {
                header.createCell(i)
                        .setCellValue(columns.get(i));
            }

            int rowNum = 1;

            for (Map<String, Object> rowData : data) {

                Row row = sheet.createRow(rowNum++);

                for (int i = 0; i < columns.size(); i++) {

                    Object value = rowData.get(columns.get(i));

                    row.createCell(i)
                            .setCellValue(
                                    value == null
                                            ? ""
                                            : value.toString()
                            );
                }
            }
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        workbook.write(out);
        workbook.close();

        return new ByteArrayInputStream(out.toByteArray());
    }
}

