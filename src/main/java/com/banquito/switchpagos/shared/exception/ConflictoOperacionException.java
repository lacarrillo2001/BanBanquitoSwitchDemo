package com.banquito.switchpagos.shared.exception;

public class ConflictoOperacionException extends SwitchPagosException {

    public ConflictoOperacionException(String codigo, String mensaje) {
        super(codigo, mensaje);
    }
}
