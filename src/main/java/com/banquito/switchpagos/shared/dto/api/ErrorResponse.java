package com.banquito.switchpagos.shared.dto.api;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

public class ErrorResponse {

    private OffsetDateTime timestamp;
    private Integer status;
    private String error;
    private String codigo;
    private String mensaje;
    private String path;
    private List<ErrorDetalle> detalles;

    public ErrorResponse() {
        this.detalles = new ArrayList<>();
    }

    public ErrorResponse(OffsetDateTime timestamp, Integer status, String error, String codigo, String mensaje,
                         String path, List<ErrorDetalle> detalles) {
        this.timestamp = timestamp;
        this.status = status;
        this.error = error;
        this.codigo = codigo;
        this.mensaje = mensaje;
        this.path = path;
        this.detalles = detalles;
    }

    public OffsetDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(OffsetDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public List<ErrorDetalle> getDetalles() {
        return detalles;
    }

    public void setDetalles(List<ErrorDetalle> detalles) {
        this.detalles = detalles;
    }
}
