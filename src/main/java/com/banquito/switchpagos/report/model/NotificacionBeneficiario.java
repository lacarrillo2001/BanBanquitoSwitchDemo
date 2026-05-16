package com.banquito.switchpagos.report.model;

import com.banquito.switchpagos.processing.model.LineaPago;
import com.banquito.switchpagos.report.enums.EstadoEnvioNotificacion;
import com.banquito.switchpagos.report.enums.TipoNotificacion;
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
@Table(schema = "switch_banquito", name = "NOTIFICACION_BENEFICIARIO")
public class NotificacionBeneficiario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_notificacion")
    private Long idNotificacion;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_linea", referencedColumnName = "id_linea")
    private LineaPago lineaPago;
    @Column(name = "correo_destino")
    private String correoDestino;
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_notificacion")
    private TipoNotificacion tipoNotificacion;
    @Column(name = "asunto")
    private String asunto;
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "contenido")
    private JsonNode contenido;
    @Enumerated(EnumType.STRING)
    @Column(name = "estado_envio")
    private EstadoEnvioNotificacion estadoEnvio;
    @Column(name = "fecha_envio")
    private OffsetDateTime fechaEnvio;
    @Column(name = "error_envio")
    private String errorEnvio;
    @Column(name = "reintentos")
    private Integer reintentos;
    @Column(name = "proximo_reintento_en")
    private OffsetDateTime proximoReintentoEn;
    @Column(name = "fecha_actualizacion")
    private OffsetDateTime fechaActualizacion;
    @Version
    @Column(name = "version")
    private Integer version;

    public NotificacionBeneficiario() {
    }

    public NotificacionBeneficiario(Long idNotificacion) {
        this.idNotificacion = idNotificacion;
    }

    public Long getIdNotificacion() { return idNotificacion; }
    public void setIdNotificacion(Long idNotificacion) { this.idNotificacion = idNotificacion; }
    public LineaPago getLineaPago() { return lineaPago; }
    public void setLineaPago(LineaPago lineaPago) { this.lineaPago = lineaPago; }
    public String getCorreoDestino() { return correoDestino; }
    public void setCorreoDestino(String correoDestino) { this.correoDestino = correoDestino; }
    public TipoNotificacion getTipoNotificacion() { return tipoNotificacion; }
    public void setTipoNotificacion(TipoNotificacion tipoNotificacion) { this.tipoNotificacion = tipoNotificacion; }
    public String getAsunto() { return asunto; }
    public void setAsunto(String asunto) { this.asunto = asunto; }
    public JsonNode getContenido() { return contenido; }
    public void setContenido(JsonNode contenido) { this.contenido = contenido; }
    public EstadoEnvioNotificacion getEstadoEnvio() { return estadoEnvio; }
    public void setEstadoEnvio(EstadoEnvioNotificacion estadoEnvio) { this.estadoEnvio = estadoEnvio; }
    public OffsetDateTime getFechaEnvio() { return fechaEnvio; }
    public void setFechaEnvio(OffsetDateTime fechaEnvio) { this.fechaEnvio = fechaEnvio; }
    public String getErrorEnvio() { return errorEnvio; }
    public void setErrorEnvio(String errorEnvio) { this.errorEnvio = errorEnvio; }
    public Integer getReintentos() { return reintentos; }
    public void setReintentos(Integer reintentos) { this.reintentos = reintentos; }
    public OffsetDateTime getProximoReintentoEn() { return proximoReintentoEn; }
    public void setProximoReintentoEn(OffsetDateTime proximoReintentoEn) { this.proximoReintentoEn = proximoReintentoEn; }
    public OffsetDateTime getFechaActualizacion() { return fechaActualizacion; }
    public void setFechaActualizacion(OffsetDateTime fechaActualizacion) { this.fechaActualizacion = fechaActualizacion; }
    public Integer getVersion() { return version; }
    public void setVersion(Integer version) { this.version = version; }

    @Override
    public boolean equals(Object objeto) {
        if (this == objeto) {
            return true;
        }
        if (!(objeto instanceof NotificacionBeneficiario notificacionBeneficiario)) {
            return false;
        }
        return idNotificacion != null && Objects.equals(idNotificacion, notificacionBeneficiario.idNotificacion);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idNotificacion);
    }

    @Override
    public String toString() {
        return "NotificacionBeneficiario{" +
                "idNotificacion=" + idNotificacion +
                ", correoDestino='" + correoDestino + '\'' +
                ", tipoNotificacion=" + tipoNotificacion +
                ", estadoEnvio=" + estadoEnvio +
                '}';
    }
}
