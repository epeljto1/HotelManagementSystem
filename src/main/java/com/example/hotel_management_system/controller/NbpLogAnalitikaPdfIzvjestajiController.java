package com.example.hotel_management_system.controller;

import com.example.hotel_management_system.model.NbpLogAnalitikaPdfIzvjestaj;
import com.example.hotel_management_system.service.NbpLogAnalitikaPdfIzvjestajiService;
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
@RequestMapping("/api/nbp-log-analitika-pdf-izvjestaji")
public class NbpLogAnalitikaPdfIzvjestajiController {

    private final NbpLogAnalitikaPdfIzvjestajiService service;

    public NbpLogAnalitikaPdfIzvjestajiController(NbpLogAnalitikaPdfIzvjestajiService service) {
        this.service = service;
    }

    @PostMapping("/generate")
    public ResponseEntity<Map<String, Object>> generateAndSave() {
        NbpLogAnalitikaPdfIzvjestaj saved = service.generateAndSaveReport();

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("message", "PDF log analitika izvjestaj uspjesno generisan i pohranjen u NBP_LOG_ANALITIKA_PDF_IZVJESTAJI.");
        body.put("id", saved.getId());
        body.put("datumGenerisanja", saved.getDatumGenerisanja());
        body.put("preuzimanje", "/api/nbp-log-analitika-pdf-izvjestaji/download");

        return ResponseEntity.ok(body);
    }

    @GetMapping("/download")
    public ResponseEntity<?> downloadPdf() {
        try {
            byte[] pdfContent = service.getLatestPdf();

            HttpHeaders headers = new HttpHeaders();
            headers.add(
                    HttpHeaders.CONTENT_DISPOSITION,
                    "attachment; filename=\"nbp_log_analitika_nbpt7.pdf\""
            );

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
