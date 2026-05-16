package com.banquito.switchpagos.shared.exception;

public class SolicitudInvalidaException extends SwitchPagosException {

    public SolicitudInvalidaException(String codigo, String mensaje) {
        super(codigo, mensaje);
    }

    public SolicitudInvalidaException(String codigo, String mensaje, Throwable causa) {
        super(codigo, mensaje, causa);
    }
}
