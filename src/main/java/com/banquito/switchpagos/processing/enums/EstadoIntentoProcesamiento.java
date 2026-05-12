package com.banquito.switchpagos.processing.enums;

import lombok.Getter;

@Getter
public enum EstadoIntentoProcesamiento {
    INICIADO("INICIADO"),
    COMPLETADO("COMPLETADO"),
    FALLIDO("FALLIDO"),
    CANCELADO("CANCELADO");

    private final String value;

    EstadoIntentoProcesamiento(String value) {
        this.value = value;
    }
}
