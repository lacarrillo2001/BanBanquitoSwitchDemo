package com.banquito.switchpagos.procesamiento.model;

import com.banquito.switchpagos.lote.model.LotePago;
import com.banquito.switchpagos.procesamiento.enums.EstadoLineaPago;
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
    @Column(name = "ID_LINEA")
    private Long idLinea;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_LOTE")
    private LotePago lotePago;
    @Column(name = "SECUENCIAL")
    private Integer secuencial;
    @Column(name = "IDENTIFICACION_BENEFICIARIO")
    private String identificacionBeneficiario;
    @Column(name = "NOMBRE_BENEFICIARIO")
    private String nombreBeneficiario;
    @Column(name = "CUENTA_DESTINO")
    private String cuentaDestino;
    @Column(name = "MONTO")
    private BigDecimal monto;
    @Column(name = "CONCEPTO_REFERENCIA")
    private String conceptoReferencia;
    @Column(name = "CORREO_NOTIFICACION")
    private String correoNotificacion;
    @Enumerated(EnumType.STRING)
    @Column(name = "ESTADO")
    private EstadoLineaPago estado;
    @Column(name = "CODIGO_ERROR")
    private String codigoError;
    @Column(name = "MENSAJE_ERROR")
    private String mensajeError;
    @Column(name = "UUID_OPERACION_SWITCH")
    private UUID uuidOperacionSwitch;
    @Column(name = "UUID_DEBITO_CORE")
    private UUID uuidDebitoCore;
    @Column(name = "UUID_CREDITO_CORE")
    private UUID uuidCreditoCore;
    @Column(name = "UUID_GRUPO_CORE")
    private UUID uuidGrupoCore;
    @Column(name = "FECHA_VALIDACION")
    private OffsetDateTime fechaValidacion;
    @Column(name = "FECHA_ENVIO_CORE")
    private OffsetDateTime fechaEnvioCore;
    @Column(name = "FECHA_RESPUESTA_CORE")
    private OffsetDateTime fechaRespuestaCore;
    @Column(name = "FECHA_PROCESO")
    private OffsetDateTime fechaProceso;
    @Column(name = "FECHA_ACTUALIZACION")
    private OffsetDateTime fechaActualizacion;
    @Version
    @Column(name = "VERSION")
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
