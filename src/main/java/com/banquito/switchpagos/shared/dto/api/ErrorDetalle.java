package com.banquito.switchpagos.shared.dto.api;

public class ErrorDetalle {

    private String campo;
    private String mensaje;

    public ErrorDetalle() {
    }

    public ErrorDetalle(String campo, String mensaje) {
        this.campo = campo;
        this.mensaje = mensaje;
    }

    public String getCampo() {
        return campo;
    }

    public void setCampo(String campo) {
        this.campo = campo;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }
}
