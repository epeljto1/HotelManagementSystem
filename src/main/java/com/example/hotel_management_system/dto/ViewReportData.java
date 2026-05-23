package com.example.hotel_management_system.dto;

import java.util.List;

public record ViewReportData(List<String> headers, List<List<String>> rows) {
}
