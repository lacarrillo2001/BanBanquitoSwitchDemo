package com.banquito.switchpagos.batch.enums;

import lombok.Getter;

@Getter
public enum EstadoLote {
    RECIBIDO("RECIBIDO"),
    VALIDANDO("VALIDANDO"),
    VALIDADO("VALIDADO"),
    RECHAZADO("RECHAZADO"),
    ENCOLADO("ENCOLADO"),
    PROCESANDO("PROCESANDO"),
    PROCESADO_PARCIAL("PROCESADO_PARCIAL"),
    PROCESADO_TOTAL("PROCESADO_TOTAL"),
    FALLIDO("FALLIDO"),
    CERRADO("CERRADO"),
    ANULADO("ANULADO");

    private final String value;

    EstadoLote(String value) {
        this.value = value;
    }
}
