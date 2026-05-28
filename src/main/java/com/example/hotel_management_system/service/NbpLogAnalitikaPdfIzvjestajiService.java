package com.example.hotel_management_system.service;

import com.example.hotel_management_system.config.DbConfig;
import com.example.hotel_management_system.dto.ViewReportData;
import com.example.hotel_management_system.model.NbpLogAnalitikaPdfIzvjestaj;
import com.example.hotel_management_system.repository.NbpLogAnalitikaPdfIzvjestajiRepository;
import com.example.hotel_management_system.repository.ViewReportRepository;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;

@Service
public class NbpLogAnalitikaPdfIzvjestajiService {

    private final ViewReportRepository viewReportRepository;
    private final NbpLogAnalitikaPdfIzvjestajiRepository repository;
    private final PdfTableReportService pdfTableReportService;

    public NbpLogAnalitikaPdfIzvjestajiService(
            ViewReportRepository viewReportRepository,
            NbpLogAnalitikaPdfIzvjestajiRepository repository,
            PdfTableReportService pdfTableReportService) {
        this.viewReportRepository = viewReportRepository;
        this.repository = repository;
        this.pdfTableReportService = pdfTableReportService;
    }

    public NbpLogAnalitikaPdfIzvjestaj generateAndSaveReport() {
        try (Connection connection = DbConfig.getConnection()) {
            connection.setAutoCommit(false);

            ViewReportData data = viewReportRepository.fetchLogAnalitikaNbpt7(connection);
            byte[] pdf = pdfTableReportService.generateReportPdf(
                    "Log analitika NBPT7 (V_NBP_LOG_ANALITIKA_NBPT7)",
                    data
            );

            NbpLogAnalitikaPdfIzvjestaj entity = new NbpLogAnalitikaPdfIzvjestaj(
                    NbpLogAnalitikaPdfIzvjestajiRepository.DEFAULT_RECORD_ID,
                    LocalDateTime.now(),
                    pdf
            );

            repository.save(entity, connection);
            connection.commit();

            return repository.findById(
                    NbpLogAnalitikaPdfIzvjestajiRepository.DEFAULT_RECORD_ID,
                    connection
            );
        } catch (SQLException e) {
            throw new RuntimeException("Greska pri generisanju i pohrani PDF log analitika izvjestaja.", e);
        }
    }

    public byte[] getLatestPdf() {
        try (Connection connection = DbConfig.getConnection()) {
            NbpLogAnalitikaPdfIzvjestaj entity = repository.findById(
                    NbpLogAnalitikaPdfIzvjestajiRepository.DEFAULT_RECORD_ID,
                    connection
            );

            if (entity == null) {
                throw new RuntimeException(
                        "PDF log analitika izvjestaj nije generisan. Pozovite POST /api/nbp-log-analitika-pdf-izvjestaji/generate."
                );
            }

            byte[] pdf = entity.getPdfIzvjestaj();
            if (pdf == null || pdf.length == 0) {
                throw new RuntimeException("PDF log analitika izvjestaj nije dostupan.");
            }

            return pdf;
        } catch (SQLException e) {
            throw new RuntimeException("Greska pri citanju PDF log analitika izvjestaja.", e);
        }
    }
}
