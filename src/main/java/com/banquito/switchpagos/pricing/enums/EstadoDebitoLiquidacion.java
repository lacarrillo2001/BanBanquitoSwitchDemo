package com.banquito.switchpagos.pricing.enums;

import lombok.Getter;

@Getter
public enum EstadoDebitoLiquidacion {
    PENDIENTE("PENDIENTE"),
    COMPLETADO("COMPLETADO"),
    RECHAZADO("RECHAZADO"),
    REVERSADO("REVERSADO");

    private final String value;

    EstadoDebitoLiquidacion(String value) {
        this.value = value;
    }
}
