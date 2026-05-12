package com.banquito.switchpagos.report.enums;

import lombok.Getter;

@Getter
public enum FormatoReporte {
    PDF("PDF"),
    CSV("CSV"),
    XLSX("XLSX"),
    JSON("JSON");

    private final String value;

    FormatoReporte(String value) {
        this.value = value;
    }
}
