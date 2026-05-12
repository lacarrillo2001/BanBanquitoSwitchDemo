package com.banquito.switchpagos.processing.model;

import com.banquito.switchpagos.batch.model.LotePago;
import com.banquito.switchpagos.processing.enums.EstadoLineaPago;
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

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(schema = "switch_banquito", name = "LINEA_PAGO")
public class LineaPago {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_linea")
    private Long idLinea;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_lote", referencedColumnName = "id_lote")
    private LotePago lotePago;
    @Column(name = "secuencial")
    private Integer secuencial;
    @Column(name = "identificacion_beneficiario")
    private String identificacionBeneficiario;
    @Column(name = "nombre_beneficiario")
    private String nombreBeneficiario;
    @Column(name = "cuenta_destino")
    private String cuentaDestino;
    @Column(name = "monto")
    private BigDecimal monto;
    @Column(name = "concepto_referencia")
    private String conceptoReferencia;
    @Column(name = "correo_notificacion")
    private String correoNotificacion;
    @Enumerated(EnumType.STRING)
    @Column(name = "estado")
    private EstadoLineaPago estado;
    @Column(name = "codigo_error")
    private String codigoError;
    @Column(name = "mensaje_error")
    private String mensajeError;
    @Column(name = "uuid_operacion_switch")
    private UUID uuidOperacionSwitch;
    @Column(name = "uuid_debito_core")
    private UUID uuidDebitoCore;
    @Column(name = "uuid_credito_core")
    private UUID uuidCreditoCore;
    @Column(name = "uuid_grupo_core")
    private UUID uuidGrupoCore;
    @Column(name = "fecha_validacion")
    private OffsetDateTime fechaValidacion;
    @Column(name = "fecha_envio_core")
    private OffsetDateTime fechaEnvioCore;
    @Column(name = "fecha_respuesta_core")
    private OffsetDateTime fechaRespuestaCore;
    @Column(name = "fecha_proceso")
    private OffsetDateTime fechaProceso;
    @Column(name = "fecha_actualizacion")
    private OffsetDateTime fechaActualizacion;
    @Version
    @Column(name = "version")
    private Integer version;

    public LineaPago() {
    }

    public LineaPago(Long idLinea) {
        this.idLinea = idLinea;
    }

    public Long getIdLinea() { return idLinea; }
    public void setIdLinea(Long idLinea) { this.idLinea = idLinea; }
    public LotePago getLotePago() { return lotePago; }
    public void setLotePago(LotePago lotePago) { this.lotePago = lotePago; }
    public Integer getSecuencial() { return secuencial; }
    public void setSecuencial(Integer secuencial) { this.secuencial = secuencial; }
    public String getIdentificacionBeneficiario() { return identificacionBeneficiario; }
    public void setIdentificacionBeneficiario(String identificacionBeneficiario) { this.identificacionBeneficiario = identificacionBeneficiario; }
    public String getNombreBeneficiario() { return nombreBeneficiario; }
    public void setNombreBeneficiario(String nombreBeneficiario) { this.nombreBeneficiario = nombreBeneficiario; }
    public String getCuentaDestino() { return cuentaDestino; }
    public void setCuentaDestino(String cuentaDestino) { this.cuentaDestino = cuentaDestino; }
    public BigDecimal getMonto() { return monto; }
    public void setMonto(BigDecimal monto) { this.monto = monto; }
    public String getConceptoReferencia() { return conceptoReferencia; }
    public void setConceptoReferencia(String conceptoReferencia) { this.conceptoReferencia = conceptoReferencia; }
    public String getCorreoNotificacion() { return correoNotificacion; }
    public void setCorreoNotificacion(String correoNotificacion) { this.correoNotificacion = correoNotificacion; }
    public EstadoLineaPago getEstado() { return estado; }
    public void setEstado(EstadoLineaPago estado) { this.estado = estado; }
    public String getCodigoError() { return codigoError; }
    public void setCodigoError(String codigoError) { this.codigoError = codigoError; }
    public String getMensajeError() { return mensajeError; }
    public void setMensajeError(String mensajeError) { this.mensajeError = mensajeError; }
    public UUID getUuidOperacionSwitch() { return uuidOperacionSwitch; }
    public void setUuidOperacionSwitch(UUID uuidOperacionSwitch) { this.uuidOperacionSwitch = uuidOperacionSwitch; }
    public UUID getUuidDebitoCore() { return uuidDebitoCore; }
    public void setUuidDebitoCore(UUID uuidDebitoCore) { this.uuidDebitoCore = uuidDebitoCore; }
    public UUID getUuidCreditoCore() { return uuidCreditoCore; }
    public void setUuidCreditoCore(UUID uuidCreditoCore) { this.uuidCreditoCore = uuidCreditoCore; }
    public UUID getUuidGrupoCore() { return uuidGrupoCore; }
    public void setUuidGrupoCore(UUID uuidGrupoCore) { this.uuidGrupoCore = uuidGrupoCore; }
    public OffsetDateTime getFechaValidacion() { return fechaValidacion; }
    public void setFechaValidacion(OffsetDateTime fechaValidacion) { this.fechaValidacion = fechaValidacion; }
    public OffsetDateTime getFechaEnvioCore() { return fechaEnvioCore; }
    public void setFechaEnvioCore(OffsetDateTime fechaEnvioCore) { this.fechaEnvioCore = fechaEnvioCore; }
    public OffsetDateTime getFechaRespuestaCore() { return fechaRespuestaCore; }
    public void setFechaRespuestaCore(OffsetDateTime fechaRespuestaCore) { this.fechaRespuestaCore = fechaRespuestaCore; }
    public OffsetDateTime getFechaProceso() { return fechaProceso; }
    public void setFechaProceso(OffsetDateTime fechaProceso) { this.fechaProceso = fechaProceso; }
    public OffsetDateTime getFechaActualizacion() { return fechaActualizacion; }
    public void setFechaActualizacion(OffsetDateTime fechaActualizacion) { this.fechaActualizacion = fechaActualizacion; }
    public Integer getVersion() { return version; }
    public void setVersion(Integer version) { this.version = version; }

    @Override
    public boolean equals(Object objeto) {
        if (this == objeto) {
            return true;
        }
        if (!(objeto instanceof LineaPago lineaPago)) {
            return false;
        }
        return idLinea != null && Objects.equals(idLinea, lineaPago.idLinea);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idLinea);
    }

    @Override
    public String toString() {
        return "LineaPago{" +
                "idLinea=" + idLinea +
                ", secuencial=" + secuencial +
                ", nombreBeneficiario='" + nombreBeneficiario + '\'' +
                ", monto=" + monto +
                ", estado=" + estado +
                '}';
    }
}
