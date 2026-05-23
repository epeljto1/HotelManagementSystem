package com.example.hotel_management_system.service;

import com.example.hotel_management_system.dto.ViewReportData;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class PdfTableReportService {

    private static final DateTimeFormatter DATE_TIME_FORMAT =
            DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    public byte[] generateReportPdf(String title, ViewReportData data) {
        Document document = new Document(PageSize.A4.rotate());
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            PdfWriter.getInstance(document, out);
            document.open();

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8);
            Font cellFont = FontFactory.getFont(FontFactory.HELVETICA, 7);

            Paragraph titleParagraph = new Paragraph(title, titleFont);
            titleParagraph.setAlignment(Element.ALIGN_CENTER);
            titleParagraph.setSpacingAfter(8);
            document.add(titleParagraph);

            document.add(new Paragraph(
                    "Datum generisanja: " + LocalDateTime.now().format(DATE_TIME_FORMAT),
                    cellFont
            ));
            document.add(new Paragraph(" ", cellFont));

            List<String> headers = data.headers();
            List<List<String>> rows = data.rows();

            if (headers.isEmpty()) {
                document.add(new Paragraph("Nema podataka za prikaz.", cellFont));
                document.close();
                return out.toByteArray();
            }

            PdfPTable table = new PdfPTable(headers.size());
            table.setWidthPercentage(100);
            table.setSpacingBefore(10f);

            for (String header : headers) {
                addCell(table, header, headerFont, true);
            }

            if (rows.isEmpty()) {
                PdfPCell emptyCell = new PdfPCell(new Phrase("Nema redova u pogledu.", cellFont));
                emptyCell.setColspan(headers.size());
                emptyCell.setPadding(6);
                table.addCell(emptyCell);
            } else {
                for (List<String> row : rows) {
                    for (int i = 0; i < headers.size(); i++) {
                        String value = i < row.size() ? row.get(i) : "";
                        addCell(table, value, cellFont, false);
                    }
                }
            }

            document.add(table);
            document.add(new Paragraph(
                    "Broj redova: " + rows.size() + " (maks. 500)",
                    cellFont
            ));
            document.close();
        } catch (DocumentException e) {
            throw new RuntimeException("Greska pri generisanju PDF izvjestaja: " + title, e);
        }

        return out.toByteArray();
    }

    private void addCell(PdfPTable table, String text, Font font, boolean header) {
        PdfPCell cell = new PdfPCell(new Phrase(text != null ? text : "", font));
        cell.setPadding(4);
        if (header) {
            cell.setBackgroundColor(java.awt.Color.LIGHT_GRAY);
        }
        table.addCell(cell);
    }
}
