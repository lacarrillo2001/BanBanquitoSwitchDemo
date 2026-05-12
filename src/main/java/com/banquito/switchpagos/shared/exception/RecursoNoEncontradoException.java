package com.banquito.switchpagos.shared.exception;

public class RecursoNoEncontradoException extends SwitchPagosException {

    public RecursoNoEncontradoException(String codigo, String mensaje) {
        super(codigo, mensaje);
    }
}
