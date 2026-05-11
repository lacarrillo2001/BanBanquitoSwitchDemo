package com.banquito.switchpagos.common.exception;

public class ConflictoOperacionException extends SwitchPagosException {

    public ConflictoOperacionException(String codigo, String mensaje) {
        super(codigo, mensaje);
    }
}
