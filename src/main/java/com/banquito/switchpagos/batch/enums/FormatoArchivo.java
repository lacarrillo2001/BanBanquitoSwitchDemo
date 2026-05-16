package com.banquito.switchpagos.batch.enums;

import lombok.Getter;

@Getter
public enum FormatoArchivo {
    CSV("CSV"),
    TXT("TXT");

    private final String value;

    FormatoArchivo(String value) {
        this.value = value;
    }
}
