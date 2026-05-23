package com.example.hotel_management_system.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PdfIzvjestaji {
    private Long id;
    private LocalDateTime datumGenerisanja;
    private byte[] pdfRezervacije;
    private byte[] pdfFakture;
    private byte[] pdfUsluge;
    private byte[] pdfLoyalty;
}
