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

/**
 * Servis zadužen za vizuelno kreiranje PDF dokumenata faktura.
 * Koristi OpenPDF biblioteku za definisanje strukture dokumenta, tabela, fontova i stilova.
 * * <p>Ovaj servis ne komunicira direktno sa bazom podataka, već prima obrađene podatke
 * putem {@link CheckOutResponseDTO} i pretvara ih u niz bajtova (byte[]) pogodan za
 * prikaz u browseru ili spašavanje u BLOB kolonu.</p>
 * * @author Tvoje Ime
 * @version 1.0
 */
@Service
public class PdfInvoiceService {

    /**
     * Generiše PDF račun sa detaljnim stavkama smještaja i dodatnih usluga u A4 formatu.
     * * <p>Metoda uključuje:</p>
     * <ul>
     * <li>Zaglavlje sa brojem računa i podacima o gostu.</li>
     * <li>Konverziju UTC vremena u lokalnu vremensku zonu (Europe/Sarajevo).</li>
     * <li>Tabelarni prikaz stavki (Smještaj, Usluge, Popusti).</li>
     * <li>Istaknuti finalni iznos (Total) sa sivom pozadinom.</li>
     * </ul>
     *
     * @param data DTO objekt koji sadrži sve finalne obračunate cifre i podatke o gostu.
     * @return byte[] Niz bajtova koji predstavljaju PDF dokument.
     */
    public byte[] generateInvoicePdfBytes(CheckOutResponseDTO data) {
        Document document = new Document(PageSize.A4);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            PdfWriter.getInstance(document, out);
            document.open();

            // Fontovi za različite nivoe teksta
            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
            Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 12);
            Font boldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);

            // Dodavanje naslova dokumenta
            Paragraph title = new Paragraph("HOTEL MANAGEMENT SYSTEM - RACUN", headerFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(20);
            document.add(title);

            // Sekcija sa osnovnim podacima
            document.add(new Paragraph("Broj racuna: #INV-" + (data.getInvoiceId() != null ? data.getInvoiceId() : "PRO-FORMA"), boldFont));

            // Konverzija vremena iz baze u lokalnu zonu za prikaz na računu
            ZonedDateTime localTime = data.getCheckOutTime()
                    .atZone(ZoneId.of("UTC"))
                    .withZoneSameInstant(ZoneId.of("Europe/Sarajevo"));

            document.add(new Paragraph("Datum odjave: " +
                    localTime.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")),
                    normalFont));

            document.add(new Paragraph("Gost: " + data.getGuestFullName(), normalFont));
            document.add(new Paragraph("Soba: " + data.getRoomNumber() + " (" + data.getRoomTypeName() + ")", normalFont));
            document.add(new Paragraph("Broj nocenja: " + data.getNumberOfNights(), normalFont));
            document.add(new Paragraph(" ", normalFont)); // Prazan red za vizuelni razmak

            // Inicijalizacija tabele sa 2 kolone (Opis i Iznos)
            PdfPTable table = new PdfPTable(2);
            table.setWidthPercentage(100);
            table.setSpacingBefore(10f);
            table.setSpacingAfter(10f);

            // Definisanje zaglavlja tabele
            addTableCell(table, "Opis stavke", boldFont);
            addTableCell(table, "Iznos (KM)", boldFont);

            // 1. Dodavanje stavke smještaja
            addTableCell(table, "Smjestaj (" + data.getNumberOfNights() + " noci)", normalFont);
            addTableCell(table, data.getAccommodationCost().toString() + " KM", normalFont);

            // 2. Dodavanje stavki za dodatne usluge (iteracija kroz listu)
            if (data.getServiceDetails() != null && !data.getServiceDetails().isEmpty()) {
                for (ServiceUsageDTO service : data.getServiceDetails()) {
                    String serviceDesc = (service.getServiceName() != null ? service.getServiceName() : "Usluga")
                            + " (x" + service.getQuantity() + ")";

                    addTableCell(table, serviceDesc, normalFont);
                    addTableCell(table, service.getTotalPrice().toString() + " KM", normalFont);
                }
            } else if (data.getAdditionalServicesCost() != null && data.getAdditionalServicesCost().doubleValue() > 0) {
                // Rezervna opcija ako lista detalja nije dostupna
                addTableCell(table, "Dodatne usluge (ukupno)", normalFont);
                addTableCell(table, data.getAdditionalServicesCost().toString() + " KM", normalFont);
            }

            // 3. Dodavanje stavke popusta sa negativnim predznakom
            if (data.getDiscountAmount() != null && data.getDiscountAmount().doubleValue() > 0) {
                String discountLabel = "Popust (" + (data.getDiscountName() != null ? data.getDiscountName() : "Akcija") + ")";
                addTableCell(table, discountLabel, normalFont);
                addTableCell(table, "-" + data.getDiscountAmount().toString() + " KM", normalFont);
            }

            // Finalni obračun (Total) sa vizuelnim isticanjem
            PdfPCell totalLabel = new PdfPCell(new Phrase("UKUPNO ZA UPLATU:", boldFont));
            totalLabel.setPadding(8);
            totalLabel.setBackgroundColor(java.awt.Color.LIGHT_GRAY);
            table.addCell(totalLabel);

            PdfPCell totalValue = new PdfPCell(new Phrase(data.getFinalAmount().toString() + " KM", boldFont));
            totalValue.setPadding(8);
            totalValue.setBackgroundColor(java.awt.Color.LIGHT_GRAY);
            table.addCell(totalValue);

            document.add(table);
            document.close();
        } catch (DocumentException e) {
            // Logovanje greške u slučaju neuspjelog kreiranja PDF strukture
            e.printStackTrace();
        }

        return out.toByteArray();
    }

    /**
     * Pomoćna metoda za uniformno dodavanje ćelija u tabelu.
     * Postavlja standardni padding i font za svaku ćeliju.
     * * @param table Tabela u koju se dodaje ćelija.
     * @param text Tekstualni sadržaj ćelije.
     * @param font Stil fonta koji će se primijeniti na tekst.
     */
    private void addTableCell(PdfPTable table, String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setPadding(8);
        table.addCell(cell);
    }
}