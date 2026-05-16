package com.banquito.switchpagos.shared.exception;

public abstract class SwitchPagosException extends RuntimeException {

    private final String codigo;

    protected SwitchPagosException(String codigo, String mensaje) {
        super(mensaje);
        this.codigo = codigo;
    }

    protected SwitchPagosException(String codigo, String mensaje, Throwable causa) {
        super(mensaje, causa);
        this.codigo = codigo;
    }

    public String getCodigo() {
        return codigo;
    }
}
