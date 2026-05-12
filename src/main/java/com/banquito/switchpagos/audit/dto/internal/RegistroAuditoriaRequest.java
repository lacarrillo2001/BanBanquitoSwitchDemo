package com.banquito.switchpagos.audit.dto.internal;

import com.banquito.switchpagos.audit.enums.TipoActorAuditoria;
import com.fasterxml.jackson.databind.JsonNode;

public class RegistroAuditoriaRequest {

    private TipoActorAuditoria tipoActor;
    private String idActor;
    private String rucEmpresa;
    private String accion;
    private String entidad;
    private String idEntidad;
    private JsonNode datosAntes;
    private JsonNode datosDespues;
    private String direccionIp;
    private String agenteUsuario;

    public RegistroAuditoriaRequest() {
    }

    public TipoActorAuditoria getTipoActor() {
        return tipoActor;
    }

    public void setTipoActor(TipoActorAuditoria tipoActor) {
        this.tipoActor = tipoActor;
    }

    public String getIdActor() {
        return idActor;
    }

    public void setIdActor(String idActor) {
        this.idActor = idActor;
    }

    public String getRucEmpresa() {
        return rucEmpresa;
    }

    public void setRucEmpresa(String rucEmpresa) {
        this.rucEmpresa = rucEmpresa;
    }

    public String getAccion() {
        return accion;
    }

    public void setAccion(String accion) {
        this.accion = accion;
    }

    public String getEntidad() {
        return entidad;
    }

    public void setEntidad(String entidad) {
        this.entidad = entidad;
    }

    public String getIdEntidad() {
        return idEntidad;
    }

    public void setIdEntidad(String idEntidad) {
        this.idEntidad = idEntidad;
    }

    public JsonNode getDatosAntes() {
        return datosAntes;
    }

    public void setDatosAntes(JsonNode datosAntes) {
        this.datosAntes = datosAntes;
    }

    public JsonNode getDatosDespues() {
        return datosDespues;
    }

    public void setDatosDespues(JsonNode datosDespues) {
        this.datosDespues = datosDespues;
    }

    public String getDireccionIp() {
        return direccionIp;
    }

    public void setDireccionIp(String direccionIp) {
        this.direccionIp = direccionIp;
    }

    public String getAgenteUsuario() {
        return agenteUsuario;
    }

    public void setAgenteUsuario(String agenteUsuario) {
        this.agenteUsuario = agenteUsuario;
    }
}
