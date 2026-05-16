package com.banquito.switchpagos.pricing.enums;

import lombok.Getter;

@Getter
public enum EstadoTarifaServicio {
    ACTIVA("ACTIVA"),
    INACTIVA("INACTIVA");

    private final String value;

    EstadoTarifaServicio(String value) {
        this.value = value;
    }
}
