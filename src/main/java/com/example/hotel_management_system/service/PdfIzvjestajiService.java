package com.example.hotel_management_system.service;

import com.example.hotel_management_system.config.DbConfig;
import com.example.hotel_management_system.dto.ViewReportData;
import com.example.hotel_management_system.enums.PdfReportType;
import com.example.hotel_management_system.model.PdfIzvjestaji;
import com.example.hotel_management_system.repository.PdfIzvjestajiRepository;
import com.example.hotel_management_system.repository.ViewReportRepository;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;

@Service
public class PdfIzvjestajiService {

    private final ViewReportRepository viewReportRepository;
    private final PdfIzvjestajiRepository pdfIzvjestajiRepository;
    private final PdfTableReportService pdfTableReportService;

    public PdfIzvjestajiService(
            ViewReportRepository viewReportRepository,
            PdfIzvjestajiRepository pdfIzvjestajiRepository,
            PdfTableReportService pdfTableReportService) {
        this.viewReportRepository = viewReportRepository;
        this.pdfIzvjestajiRepository = pdfIzvjestajiRepository;
        this.pdfTableReportService = pdfTableReportService;
    }

    public PdfIzvjestaji generateAndSaveAllReports() {
        try (Connection connection = DbConfig.getConnection()) {
            connection.setAutoCommit(false);

            byte[] pdfRezervacije = buildPdf(
                    "Rezervacijski pregled (V_NBP_REZERVACIJSKI_PREGLED)",
                    viewReportRepository.fetchRezervacijskiPregled(connection)
            );
            byte[] pdfFakture = buildPdf(
                    "Faktura detalji (V_NBP_FAKTURA_DETALJI)",
                    viewReportRepository.fetchFakturaDetalji(connection)
            );
            byte[] pdfUsluge = buildPdf(
                    "Pregled usluga (V_NBP_USLUGA_PREGLED)",
                    viewReportRepository.fetchUslugaPregled(connection)
            );
            byte[] pdfLoyalty = buildPdf(
                    "Loyalty profil gostiju (V_GUEST_LOYALTY_PROFILE)",
                    viewReportRepository.fetchGuestLoyaltyProfile(connection)
            );

            PdfIzvjestaji entity = new PdfIzvjestaji(
                    PdfIzvjestajiRepository.DEFAULT_RECORD_ID,
                    LocalDateTime.now(),
                    pdfRezervacije,
                    pdfFakture,
                    pdfUsluge,
                    pdfLoyalty
            );

            pdfIzvjestajiRepository.save(entity, connection);
            connection.commit();

            return pdfIzvjestajiRepository.findById(PdfIzvjestajiRepository.DEFAULT_RECORD_ID, connection);
        } catch (SQLException e) {
            throw new RuntimeException("Greska pri generisanju i pohrani PDF izvjestaja.", e);
        }
    }

    public byte[] getPdfByType(PdfReportType type) {
        try (Connection connection = DbConfig.getConnection()) {
            PdfIzvjestaji entity = pdfIzvjestajiRepository.findById(
                    PdfIzvjestajiRepository.DEFAULT_RECORD_ID,
                    connection
            );

            if (entity == null) {
                throw new RuntimeException(
                        "PDF izvjestaji nisu generisani. Pozovite POST /api/pdf-izvjestaji/generate."
                );
            }

            byte[] pdf = switch (type) {
                case REZERVACIJE -> entity.getPdfRezervacije();
                case FAKTURE -> entity.getPdfFakture();
                case USLUGE -> entity.getPdfUsluge();
                case LOYALTY -> entity.getPdfLoyalty();
            };

            if (pdf == null || pdf.length == 0) {
                throw new RuntimeException("PDF za tip '" + type.getPathKey() + "' nije dostupan.");
            }

            return pdf;
        } catch (SQLException e) {
            throw new RuntimeException("Greska pri citanju PDF izvjestaja.", e);
        }
    }

    public PdfIzvjestaji getMetadata() {
        try (Connection connection = DbConfig.getConnection()) {
            PdfIzvjestaji entity = pdfIzvjestajiRepository.findById(
                    PdfIzvjestajiRepository.DEFAULT_RECORD_ID,
                    connection
            );
            if (entity == null) {
                return null;
            }
            entity.setPdfRezervacije(null);
            entity.setPdfFakture(null);
            entity.setPdfUsluge(null);
            entity.setPdfLoyalty(null);
            return entity;
        } catch (SQLException e) {
            throw new RuntimeException("Greska pri citanju metapodataka PDF izvjestaja.", e);
        }
    }

    private byte[] buildPdf(String title, ViewReportData data) {
        return pdfTableReportService.generateReportPdf(title, data);
    }
}
