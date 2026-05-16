package com.banquito.switchpagos.catalog.enums;

import lombok.Getter;

@Getter
public enum EstadoTipoServicio {
    ACTIVO("ACTIVO"),
    INACTIVO("INACTIVO");

    private final String value;

    EstadoTipoServicio(String value) {
        this.value = value;
    }
}
