package com.banquito.switchpagos.batch.enums;

import lombok.Getter;

@Getter
public enum EstadoColaProcesamiento {
    PENDIENTE("PENDIENTE"),
    TOMADO("TOMADO"),
    PROCESANDO("PROCESANDO"),
    COMPLETADO("COMPLETADO"),
    FALLIDO("FALLIDO"),
    REINTENTO("REINTENTO"),
    CANCELADO("CANCELADO");

    private final String value;

    EstadoColaProcesamiento(String value) {
        this.value = value;
    }
}
