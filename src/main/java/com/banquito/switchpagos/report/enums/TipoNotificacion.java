package com.banquito.switchpagos.report.enums;

import lombok.Getter;

@Getter
public enum TipoNotificacion {
    PAGO_EXITOSO("PAGO_EXITOSO"),
    PAGO_RECHAZADO("PAGO_RECHAZADO"),
    PAGO_REVERSADO("PAGO_REVERSADO");

    private final String value;

    TipoNotificacion(String value) {
        this.value = value;
    }
}
