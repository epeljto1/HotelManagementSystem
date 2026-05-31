package com.example.hotel_management_system.service;

import com.example.hotel_management_system.dto.XmlExportDTO;
import com.example.hotel_management_system.repository.XmlExportRepository;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class XmlExportService {

    private final XmlExportRepository xmlExportRepository;

    public XmlExportService(XmlExportRepository xmlExportRepository) {
        this.xmlExportRepository = xmlExportRepository;
    }

    public String exportDataToXml() throws Exception {
        List<XmlExportDTO> data = xmlExportRepository.getExportData();

        XmlMapper xmlMapper = new XmlMapper();

        return xmlMapper.writeValueAsString(data);
    }
}