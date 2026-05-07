package org.enterprise.reportengine.service;

import com.lowagie.text.Document;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class PdfExportService {

    public ByteArrayInputStream export(
            List<Map<String, Object>> data
    ) throws Exception {

        Document document = new Document();

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        PdfWriter.getInstance(document, out);

        document.open();

        if (!data.isEmpty()) {

            List<String> columns =
                    new ArrayList<>(data.get(0).keySet());

            PdfPTable table = new PdfPTable(columns.size());

            for (String column : columns) {
                table.addCell(column);
            }

            for (Map<String, Object> row : data) {

                for (String column : columns) {

                    Object value = row.get(column);

                    table.addCell(
                            value == null
                                    ? ""
                                    : value.toString()
                    );
                }
            }

            document.add(table);
        }

        document.close();

        return new ByteArrayInputStream(out.toByteArray());
    }
}
