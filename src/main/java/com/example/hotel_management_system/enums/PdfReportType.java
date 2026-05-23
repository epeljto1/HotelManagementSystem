package com.example.hotel_management_system.enums;

public enum PdfReportType {
    REZERVACIJE("rezervacije", "PDF_REZERVACIJE"),
    FAKTURE("fakture", "PDF_FAKTURE"),
    USLUGE("usluge", "PDF_USLUGE"),
    LOYALTY("loyalty", "PDF_LOYALTY");

    private final String pathKey;
    private final String columnName;

    PdfReportType(String pathKey, String columnName) {
        this.pathKey = pathKey;
        this.columnName = columnName;
    }

    public String getPathKey() {
        return pathKey;
    }

    public String getColumnName() {
        return columnName;
    }

    public static PdfReportType fromPathKey(String key) {
        for (PdfReportType type : values()) {
            if (type.pathKey.equalsIgnoreCase(key)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Nepoznat tip PDF izvjestaja: " + key);
    }
}
