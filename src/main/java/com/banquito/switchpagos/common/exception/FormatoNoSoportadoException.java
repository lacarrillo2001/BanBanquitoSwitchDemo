package com.banquito.switchpagos.common.exception;

public class FormatoNoSoportadoException extends SwitchPagosException {

    public FormatoNoSoportadoException(String codigo, String mensaje) {
        super(codigo, mensaje);
    }
}
