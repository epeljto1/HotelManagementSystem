package com.example.hotel_management_system.controller;

import com.example.hotel_management_system.service.XmlExportService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/xml")
public class XmlExportController {

    private final XmlExportService xmlExportService;

    public XmlExportController(XmlExportService xmlExportService) {
        this.xmlExportService = xmlExportService;
    }

    @GetMapping(value = "/export", produces = MediaType.APPLICATION_XML_VALUE)
    public ResponseEntity<String> exportXml() {
        try {
            String xml = xmlExportService.exportDataToXml();
            return ResponseEntity.ok(xml);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("<error>Greška prilikom XML exporta</error>");
        }
    }
}