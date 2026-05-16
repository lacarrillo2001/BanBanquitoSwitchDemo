package com.banquito.switchpagos.pricing.enums;

import lombok.Getter;

@Getter
public enum ConceptoDetalleLiquidacion {
    DEBITO_CUENTA_MATRIZ("DEBITO_CUENTA_MATRIZ"),
    CREDITO_INGRESOS("CREDITO_INGRESOS"),
    CREDITO_IVA("CREDITO_IVA"),
    REVERSO("REVERSO");

    private final String value;

    ConceptoDetalleLiquidacion(String value) {
        this.value = value;
    }
}
