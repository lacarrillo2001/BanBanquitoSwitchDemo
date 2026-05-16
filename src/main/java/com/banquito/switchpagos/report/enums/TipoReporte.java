package com.banquito.switchpagos.report.enums;

import lombok.Getter;

@Getter
public enum TipoReporte {
    COMPROBANTE_LIQUIDACION("COMPROBANTE_LIQUIDACION"),
    REPORTE_NOVEDADES("REPORTE_NOVEDADES");

    private final String value;

    TipoReporte(String value) {
        this.value = value;
    }
}
