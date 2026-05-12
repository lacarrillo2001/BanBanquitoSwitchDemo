package com.banquito.switchpagos.report.enums;

import lombok.Getter;

@Getter
public enum EstadoEnvioNotificacion {
    PENDIENTE("PENDIENTE"),
    ENVIADA("ENVIADA"),
    ERROR("ERROR"),
    CANCELADA("CANCELADA");

    private final String value;

    EstadoEnvioNotificacion(String value) {
        this.value = value;
    }
}
