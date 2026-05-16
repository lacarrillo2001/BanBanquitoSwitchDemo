package com.banquito.switchpagos.parameter.dto.api;

public class HorarioCorteResponse {

    private String horaCorteProceso;
    private String horaInicioLotesEncolados;
    private Integer ventanaDuplicidadDias;
    private String zonaHoraria;
    private String mensaje;

    public HorarioCorteResponse() {
    }

    public HorarioCorteResponse(String horaCorteProceso, String horaInicioLotesEncolados,
                                Integer ventanaDuplicidadDias, String zonaHoraria, String mensaje) {
        this.horaCorteProceso = horaCorteProceso;
        this.horaInicioLotesEncolados = horaInicioLotesEncolados;
        this.ventanaDuplicidadDias = ventanaDuplicidadDias;
        this.zonaHoraria = zonaHoraria;
        this.mensaje = mensaje;
    }

    public String getHoraCorteProceso() {
        return horaCorteProceso;
    }

    public void setHoraCorteProceso(String horaCorteProceso) {
        this.horaCorteProceso = horaCorteProceso;
    }

    public String getHoraInicioLotesEncolados() {
        return horaInicioLotesEncolados;
    }

    public void setHoraInicioLotesEncolados(String horaInicioLotesEncolados) {
        this.horaInicioLotesEncolados = horaInicioLotesEncolados;
    }

    public Integer getVentanaDuplicidadDias() {
        return ventanaDuplicidadDias;
    }

    public void setVentanaDuplicidadDias(Integer ventanaDuplicidadDias) {
        this.ventanaDuplicidadDias = ventanaDuplicidadDias;
    }

    public String getZonaHoraria() {
        return zonaHoraria;
    }

    public void setZonaHoraria(String zonaHoraria) {
        this.zonaHoraria = zonaHoraria;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }
}
