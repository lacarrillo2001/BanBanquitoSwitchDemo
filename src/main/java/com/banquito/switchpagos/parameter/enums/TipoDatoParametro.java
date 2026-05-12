package com.banquito.switchpagos.parameter.enums;

import lombok.Getter;

@Getter
public enum TipoDatoParametro {
    NUMERICO("NUMERICO"),
    CADENA("CADENA"),
    FECHA("FECHA"),
    HORA("HORA"),
    BOOLEANO("BOOLEANO"),
    JSON("JSON");

    private final String value;

    TipoDatoParametro(String value) {
        this.value = value;
    }
}
