package com.example.hotel_management_system.controller;

import com.example.hotel_management_system.enums.PdfReportType;
import com.example.hotel_management_system.model.PdfIzvjestaji;
import com.example.hotel_management_system.service.PdfIzvjestajiService;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayInputStream;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/pdf-izvjestaji")
public class PdfIzvjestajiController {

    private final PdfIzvjestajiService service;

    public PdfIzvjestajiController(PdfIzvjestajiService service) {
        this.service = service;
    }

    @PostMapping("/generate")
    public ResponseEntity<Map<String, Object>> generateAndSave() {
        PdfIzvjestaji saved = service.generateAndSaveAllReports();
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("message", "PDF izvjestaji uspjesno generisani i pohranjeni u PDF_IZVJESTAJI.");
        body.put("id", saved.getId());
        body.put("datumGenerisanja", saved.getDatumGenerisanja());
        body.put("preuzimanje", Map.of(
                "rezervacije", "/api/pdf-izvjestaji/rezervacije",
                "fakture", "/api/pdf-izvjestaji/fakture",
                "usluge", "/api/pdf-izvjestaji/usluge",
                "loyalty", "/api/pdf-izvjestaji/loyalty"
        ));
        return ResponseEntity.ok(body);
    }

    @GetMapping("/metadata")
    public ResponseEntity<?> getMetadata() {
        PdfIzvjestaji metadata = service.getMetadata();
        if (metadata == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "code", "PDF_NOT_FOUND",
                    "message", "Nema pohranjenih PDF izvjestaja. Pozovite POST /api/pdf-izvjestaji/generate."
            ));
        }
        return ResponseEntity.ok(metadata);
    }

    @GetMapping("/rezervacije")
    public ResponseEntity<?> downloadRezervacijePdf() {
        return downloadPdf(PdfReportType.REZERVACIJE);
    }

    @GetMapping("/fakture")
    public ResponseEntity<?> downloadFakturePdf() {
        return downloadPdf(PdfReportType.FAKTURE);
    }

    @GetMapping("/usluge")
    public ResponseEntity<?> downloadUslugePdf() {
        return downloadPdf(PdfReportType.USLUGE);
    }

    @GetMapping("/loyalty")
    public ResponseEntity<?> downloadLoyaltyPdf() {
        return downloadPdf(PdfReportType.LOYALTY);
    }

    private ResponseEntity<?> downloadPdf(PdfReportType reportType) {
        try {
            byte[] pdfContent = service.getPdfByType(reportType);
            String filename = "izvjestaj_" + reportType.getPathKey() + ".pdf";

            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"");

            return ResponseEntity.ok()
                    .headers(headers)
                    .contentType(MediaType.APPLICATION_PDF)
                    .contentLength(pdfContent.length)
                    .body(new InputStreamResource(new ByteArrayInputStream(pdfContent)));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "code", "PDF_NOT_FOUND",
                    "message", e.getMessage()
            ));
        }
    }
}
