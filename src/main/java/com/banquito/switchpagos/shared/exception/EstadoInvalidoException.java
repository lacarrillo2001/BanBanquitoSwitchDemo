package com.banquito.switchpagos.shared.exception;

public class EstadoInvalidoException extends SwitchPagosException {

    public EstadoInvalidoException(String codigo, String mensaje) {
        super(codigo, mensaje);
    }
}
