package com.banquito.switchpagos.procesamiento.model;

import com.banquito.switchpagos.lote.model.ColaProcesamiento;
import com.banquito.switchpagos.procesamiento.enums.EstadoIntentoProcesamiento;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.Objects;

@Entity
@Table(schema = "switch_banquito", name = "INTENTO_PROCESAMIENTO")
public class IntentoProcesamiento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_INTENTO")
    private Long idIntento;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_COLA")
    private ColaProcesamiento colaProcesamiento;
    @Column(name = "NUMERO_INTENTO")
    private Integer numeroIntento;
    @Column(name = "FECHA_INICIO")
    private OffsetDateTime fechaInicio;
    @Column(name = "FECHA_FIN")
    private OffsetDateTime fechaFin;
    @Enumerated(EnumType.STRING)
    @Column(name = "ESTADO")
    private EstadoIntentoProcesamiento estado;
    @Column(name = "CODIGO_ERROR")
    private String codigoError;
    @Column(name = "MENSAJE_ERROR")
    private String mensajeError;
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "SOLICITUD_CORE")
    private JsonNode solicitudCore;
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "RESPUESTA_CORE")
    private JsonNode respuestaCore;
    @Column(name = "FECHA_ACTUALIZACION")
    private OffsetDateTime fechaActualizacion;
    @Version
    @Column(name = "VERSION")
    private Integer version;

    public IntentoProcesamiento() {
    }

    public IntentoProcesamiento(Long idIntento) {
        this.idIntento = idIntento;
    }

    public Long getIdIntento() { return idIntento; }
    public void setIdIntento(Long idIntento) { this.idIntento = idIntento; }
    public ColaProcesamiento getColaProcesamiento() { return colaProcesamiento; }
    public void setColaProcesamiento(ColaProcesamiento colaProcesamiento) { this.colaProcesamiento = colaProcesamiento; }
    public Integer getNumeroIntento() { return numeroIntento; }
    public void setNumeroIntento(Integer numeroIntento) { this.numeroIntento = numeroIntento; }
    public OffsetDateTime getFechaInicio() { return fechaInicio; }
    public void setFechaInicio(OffsetDateTime fechaInicio) { this.fechaInicio = fechaInicio; }
    public OffsetDateTime getFechaFin() { return fechaFin; }
    public void setFechaFin(OffsetDateTime fechaFin) { this.fechaFin = fechaFin; }
    public EstadoIntentoProcesamiento getEstado() { return estado; }
    public void setEstado(EstadoIntentoProcesamiento estado) { this.estado = estado; }
    public String getCodigoError() { return codigoError; }
    public void setCodigoError(String codigoError) { this.codigoError = codigoError; }
    public String getMensajeError() { return mensajeError; }
    public void setMensajeError(String mensajeError) { this.mensajeError = mensajeError; }
    public JsonNode getSolicitudCore() { return solicitudCore; }
    public void setSolicitudCore(JsonNode solicitudCore) { this.solicitudCore = solicitudCore; }
    public JsonNode getRespuestaCore() { return respuestaCore; }
    public void setRespuestaCore(JsonNode respuestaCore) { this.respuestaCore = respuestaCore; }
    public OffsetDateTime getFechaActualizacion() { return fechaActualizacion; }
    public void setFechaActualizacion(OffsetDateTime fechaActualizacion) { this.fechaActualizacion = fechaActualizacion; }
    public Integer getVersion() { return version; }
    public void setVersion(Integer version) { this.version = version; }

    @Override
    public boolean equals(Object objeto) {
        if (this == objeto) {
            return true;
        }
        if (!(objeto instanceof IntentoProcesamiento intentoProcesamiento)) {
            return false;
        }
        return idIntento != null && Objects.equals(idIntento, intentoProcesamiento.idIntento);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idIntento);
    }

    @Override
    public String toString() {
        return "IntentoProcesamiento{" +
                "idIntento=" + idIntento +
                ", numeroIntento=" + numeroIntento +
                ", estado=" + estado +
                '}';
    }
}
