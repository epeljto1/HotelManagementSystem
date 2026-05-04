package com.example.hotel_management_system.service;

import com.example.hotel_management_system.dto.CheckOutResponseDTO;
import com.example.hotel_management_system.dto.ServiceUsageDTO;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class PdfInvoiceService {

    /**
     * Generiše PDF račun sa detaljnim stavkama smještaja i dodatnih usluga.
     */
    public byte[] generateInvoicePdfBytes(CheckOutResponseDTO data) {
        Document document = new Document(PageSize.A4);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            PdfWriter.getInstance(document, out);
            document.open();

            // Fontovi
            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
            Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 12);
            Font boldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);

            // Naslov
            Paragraph title = new Paragraph("HOTEL MANAGEMENT SYSTEM - RACUN", headerFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(20);
            document.add(title);

            // Osnovni podaci o gostu i boravku
            document.add(new Paragraph("Broj racuna: #INV-" + (data.getInvoiceId() != null ? data.getInvoiceId() : "PRO-FORMA"), boldFont));

            //document.add(new Paragraph("Datum odjave: " + data.getCheckOutTime().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")), normalFont));

            ZonedDateTime localTime = data.getCheckOutTime()
                    .atZone(ZoneId.of("UTC"))
                    .withZoneSameInstant(ZoneId.of("Europe/Sarajevo"));

            document.add(new Paragraph("Datum odjave: " +
                    localTime.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")),
                    normalFont));

            document.add(new Paragraph("Gost: " + data.getGuestFullName(), normalFont));
            document.add(new Paragraph("Soba: " + data.getRoomNumber() + " (" + data.getRoomTypeName() + ")", normalFont));
            document.add(new Paragraph("Broj nocenja: " + data.getNumberOfNights(), normalFont));
            document.add(new Paragraph(" ", normalFont));

            // Tabela sa stavkama
            PdfPTable table = new PdfPTable(2);
            table.setWidthPercentage(100);
            table.setSpacingBefore(10f);
            table.setSpacingAfter(10f);

            // Zaglavlje tabele
            addTableCell(table, "Opis stavke", boldFont);
            addTableCell(table, "Iznos (KM)", boldFont);

            // 1. Stavka: Smještaj (Osnovna cijena)
            addTableCell(table, "Smjestaj (" + data.getNumberOfNights() + " noci)", normalFont);
            addTableCell(table, data.getAccommodationCost().toString() + " KM", normalFont);

            // 2. Stavke: Detaljne dodatne usluge (ako postoje)
            if (data.getServiceDetails() != null && !data.getServiceDetails().isEmpty()) {
                for (ServiceUsageDTO service : data.getServiceDetails()) {
                    // Prikazujemo naziv usluge i količinu (npr. Breakfast (x2))
                    String serviceDesc = (service.getServiceName() != null ? service.getServiceName() : "Usluga")
                            + " (x" + service.getQuantity() + ")";

                    addTableCell(table, serviceDesc, normalFont);
                    addTableCell(table, service.getTotalPrice().toString() + " KM", normalFont);
                }
            } else if (data.getAdditionalServicesCost() != null && data.getAdditionalServicesCost().doubleValue() > 0) {
                // Fallback ako lista detalja nije stigla, ali postoji ukupna cifra
                addTableCell(table, "Dodatne usluge (ukupno)", normalFont);
                addTableCell(table, data.getAdditionalServicesCost().toString() + " KM", normalFont);
            }

            // 3. Stavka: Popust (ako je primijenjen)
            if (data.getDiscountAmount() != null && data.getDiscountAmount().doubleValue() > 0) {
                String discountLabel = "Popust (" + (data.getDiscountName() != null ? data.getDiscountName() : "Akcija") + ")";
                addTableCell(table, discountLabel, normalFont);
                addTableCell(table, "-" + data.getDiscountAmount().toString() + " KM", normalFont);
            }

            PdfPCell totalLabel = new PdfPCell(new Phrase("UKUPNO ZA UPLATU:", boldFont));
            totalLabel.setPadding(8);
            totalLabel.setBackgroundColor(java.awt.Color.LIGHT_GRAY); // Blago siva boja za isticanje
            table.addCell(totalLabel);

            PdfPCell totalValue = new PdfPCell(new Phrase(data.getFinalAmount().toString() + " KM", boldFont));
            totalValue.setPadding(8);
            totalValue.setBackgroundColor(java.awt.Color.LIGHT_GRAY);
            table.addCell(totalValue);

            document.add(table);
            document.close();
        } catch (DocumentException e) {
            e.printStackTrace();
        }

        return out.toByteArray();
    }

    private void addTableCell(PdfPTable table, String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setPadding(8);
        table.addCell(cell);
    }
}