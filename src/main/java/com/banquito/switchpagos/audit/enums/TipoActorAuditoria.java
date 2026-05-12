package com.banquito.switchpagos.audit.enums;

import lombok.Getter;

@Getter
public enum TipoActorAuditoria {
    EMPRESA("EMPRESA"),
    USUARIO_CORE("USUARIO_CORE"),
    SISTEMA("SISTEMA"),
    API("API");

    private final String value;

    TipoActorAuditoria(String value) {
        this.value = value;
    }
}
