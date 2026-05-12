package com.banquito.switchpagos.processing.enums;

import lombok.Getter;

@Getter
public enum EstadoLineaPago {
    PENDIENTE("PENDIENTE"),
    VALIDADA("VALIDADA"),
    RECHAZADA("RECHAZADA"),
    ENVIADA_CORE("ENVIADA_CORE"),
    EXITOSA("EXITOSA"),
    FALLIDA("FALLIDA"),
    REVERSADA("REVERSADA");

    private final String value;

    EstadoLineaPago(String value) {
        this.value = value;
    }
}
