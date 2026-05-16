package com.banquito.switchpagos.audit.model;

import com.banquito.switchpagos.audit.enums.TipoActorAuditoria;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.Objects;

@Entity
@Table(schema = "switch_banquito", name = "BITACORA_AUDITORIA_SWITCH")
public class BitacoraAuditoriaSwitch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_auditoria")
    private Long idAuditoria;
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_actor")
    private TipoActorAuditoria tipoActor;
    @Column(name = "id_actor")
    private String idActor;
    @Column(name = "ruc_empresa")
    private String rucEmpresa;
    @Column(name = "accion")
    private String accion;
    @Column(name = "entidad")
    private String entidad;
    @Column(name = "id_entidad")
    private String idEntidad;
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "datos_antes")
    private JsonNode datosAntes;
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "datos_despues")
    private JsonNode datosDespues;
    @Column(name = "direccion_ip", columnDefinition = "inet", insertable = false, updatable = false)
    private String direccionIp;
    @Column(name = "agente_usuario")
    private String agenteUsuario;
    @Column(name = "fecha_creacion")
    private OffsetDateTime fechaCreacion;

    public BitacoraAuditoriaSwitch() {
    }

    public BitacoraAuditoriaSwitch(Long idAuditoria) {
        this.idAuditoria = idAuditoria;
    }

    public Long getIdAuditoria() { return idAuditoria; }
    public void setIdAuditoria(Long idAuditoria) { this.idAuditoria = idAuditoria; }
    public TipoActorAuditoria getTipoActor() { return tipoActor; }
    public void setTipoActor(TipoActorAuditoria tipoActor) { this.tipoActor = tipoActor; }
    public String getIdActor() { return idActor; }
    public void setIdActor(String idActor) { this.idActor = idActor; }
    public String getRucEmpresa() { return rucEmpresa; }
    public void setRucEmpresa(String rucEmpresa) { this.rucEmpresa = rucEmpresa; }
    public String getAccion() { return accion; }
    public void setAccion(String accion) { this.accion = accion; }
    public String getEntidad() { return entidad; }
    public void setEntidad(String entidad) { this.entidad = entidad; }
    public String getIdEntidad() { return idEntidad; }
    public void setIdEntidad(String idEntidad) { this.idEntidad = idEntidad; }
    public JsonNode getDatosAntes() { return datosAntes; }
    public void setDatosAntes(JsonNode datosAntes) { this.datosAntes = datosAntes; }
    public JsonNode getDatosDespues() { return datosDespues; }
    public void setDatosDespues(JsonNode datosDespues) { this.datosDespues = datosDespues; }
    public String getDireccionIp() { return direccionIp; }
    public void setDireccionIp(String direccionIp) { this.direccionIp = direccionIp; }
    public String getAgenteUsuario() { return agenteUsuario; }
    public void setAgenteUsuario(String agenteUsuario) { this.agenteUsuario = agenteUsuario; }
    public OffsetDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(OffsetDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    @Override
    public boolean equals(Object objeto) {
        if (this == objeto) {
            return true;
        }
        if (!(objeto instanceof BitacoraAuditoriaSwitch bitacoraAuditoriaSwitch)) {
            return false;
        }
        return idAuditoria != null && Objects.equals(idAuditoria, bitacoraAuditoriaSwitch.idAuditoria);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idAuditoria);
    }

    @Override
    public String toString() {
        return "BitacoraAuditoriaSwitch{" +
                "idAuditoria=" + idAuditoria +
                ", tipoActor=" + tipoActor +
                ", accion='" + accion + '\'' +
                ", entidad='" + entidad + '\'' +
                ", idEntidad='" + idEntidad + '\'' +
                '}';
    }
}
