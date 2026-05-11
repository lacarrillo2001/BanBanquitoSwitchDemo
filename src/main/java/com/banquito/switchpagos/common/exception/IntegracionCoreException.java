package com.banquito.switchpagos.common.exception;

public class IntegracionCoreException extends SwitchPagosException {

    public IntegracionCoreException(String codigo, String mensaje) {
        super(codigo, mensaje);
    }

    public IntegracionCoreException(String codigo, String mensaje, Throwable causa) {
        super(codigo, mensaje, causa);
    }
}
