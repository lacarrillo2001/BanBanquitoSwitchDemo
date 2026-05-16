package com.banquito.switchpagos.processing.enums;

import lombok.Getter;

@Getter
public enum EstadoLimiteTransaccion {
    ACTIVO("ACTIVO"),
    INACTIVO("INACTIVO");

    private final String value;

    EstadoLimiteTransaccion(String value) {
        this.value = value;
    }
}
