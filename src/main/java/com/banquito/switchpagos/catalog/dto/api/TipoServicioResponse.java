package com.banquito.switchpagos.catalog.dto.api;

public class TipoServicioResponse {

    private String codigo;
    private String nombre;
    private String descripcion;
    private String estado;

    public TipoServicioResponse() {
    }

    public TipoServicioResponse(String codigo, String nombre, String descripcion, String estado) {
        this.codigo = codigo;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.estado = estado;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
}
