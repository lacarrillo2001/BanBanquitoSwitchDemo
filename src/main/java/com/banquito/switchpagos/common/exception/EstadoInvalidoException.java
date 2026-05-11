package com.banquito.switchpagos.common.exception;

public class EstadoInvalidoException extends SwitchPagosException {

    public EstadoInvalidoException(String codigo, String mensaje) {
        super(codigo, mensaje);
    }
}
