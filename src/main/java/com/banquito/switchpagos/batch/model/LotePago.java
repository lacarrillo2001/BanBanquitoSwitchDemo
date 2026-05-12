package com.banquito.switchpagos.batch.model;

import com.banquito.switchpagos.catalog.model.TipoServicio;
import com.banquito.switchpagos.batch.enums.CanalIngreso;
import com.banquito.switchpagos.batch.enums.EstadoLote;
import com.banquito.switchpagos.batch.enums.FormatoArchivo;
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
@Table(schema = "switch_banquito", name = "LOTE_PAGO")
public class LotePago {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_lote")
    private Long idLote;
    @Column(name = "uuid_lote")
    private UUID uuidLote;
    @Column(name = "clave_idempotencia")
    private UUID claveIdempotencia;
    @Column(name = "ruc_empresa")
    private String rucEmpresa;
    @Column(name = "id_credencial_web_core")
    private Integer idCredencialWebCore;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tipo_servicio", referencedColumnName = "codigo")
    private TipoServicio tipoServicio;
    @Column(name = "cuenta_matriz_cargo")
    private String cuentaMatrizCargo;
    @Column(name = "fecha_hora_generacion")
    private OffsetDateTime fechaHoraGeneracion;
    @Column(name = "total_registros_declarado")
    private Integer totalRegistrosDeclarado;
    @Column(name = "monto_total_declarado")
    private BigDecimal montoTotalDeclarado;
    @Column(name = "total_registros_pie")
    private Integer totalRegistrosPie;
    @Column(name = "monto_total_pie")
    private BigDecimal montoTotalPie;
    @Column(name = "total_registros_validados")
    private Integer totalRegistrosValidados;
    @Column(name = "total_registros_rechazados")
    private Integer totalRegistrosRechazados;
    @Column(name = "monto_total_validado")
    private BigDecimal montoTotalValidado;
    @Column(name = "nombre_archivo")
    private String nombreArchivo;
    @Column(name = "hash_archivo")
    private String hashArchivo;
    @Column(name = "hash_pie_control")
    private String hashPieControl;
    @Column(name = "tamano_bytes")
    private Long tamanoBytes;
    @Enumerated(EnumType.STRING)
    @Column(name = "formato_archivo")
    private FormatoArchivo formatoArchivo;
    @Column(name = "ruta_almacenamiento")
    private String rutaAlmacenamiento;
    @Enumerated(EnumType.STRING)
    @Column(name = "canal_ingreso")
    private CanalIngreso canalIngreso;
    @Enumerated(EnumType.STRING)
    @Column(name = "estado")
    private EstadoLote estado;
    @Column(name = "motivo_rechazo_global")
    private String motivoRechazoGlobal;
    @Column(name = "fecha_recepcion")
    private OffsetDateTime fechaRecepcion;
    @Column(name = "fecha_inicio_validacion")
    private OffsetDateTime fechaInicioValidacion;
    @Column(name = "fecha_fin_validacion")
    private OffsetDateTime fechaFinValidacion;
    @Column(name = "fecha_inicio_proceso")
    private OffsetDateTime fechaInicioProceso;
    @Column(name = "fecha_fin_proceso")
    private OffsetDateTime fechaFinProceso;
    @Column(name = "fecha_cierre")
    private OffsetDateTime fechaCierre;
    @Column(name = "fecha_actualizacion")
    private OffsetDateTime fechaActualizacion;
    @Version
    @Column(name = "version")
    private Integer version;

    public LotePago() {
    }

    public LotePago(Long idLote) {
        this.idLote = idLote;
    }

    public Long getIdLote() { return idLote; }
    public void setIdLote(Long idLote) { this.idLote = idLote; }
    public UUID getUuidLote() { return uuidLote; }
    public void setUuidLote(UUID uuidLote) { this.uuidLote = uuidLote; }
    public UUID getClaveIdempotencia() { return claveIdempotencia; }
    public void setClaveIdempotencia(UUID claveIdempotencia) { this.claveIdempotencia = claveIdempotencia; }
    public String getRucEmpresa() { return rucEmpresa; }
    public void setRucEmpresa(String rucEmpresa) { this.rucEmpresa = rucEmpresa; }
    public Integer getIdCredencialWebCore() { return idCredencialWebCore; }
    public void setIdCredencialWebCore(Integer idCredencialWebCore) { this.idCredencialWebCore = idCredencialWebCore; }
    public TipoServicio getTipoServicio() { return tipoServicio; }
    public void setTipoServicio(TipoServicio tipoServicio) { this.tipoServicio = tipoServicio; }
    public String getCuentaMatrizCargo() { return cuentaMatrizCargo; }
    public void setCuentaMatrizCargo(String cuentaMatrizCargo) { this.cuentaMatrizCargo = cuentaMatrizCargo; }
    public OffsetDateTime getFechaHoraGeneracion() { return fechaHoraGeneracion; }
    public void setFechaHoraGeneracion(OffsetDateTime fechaHoraGeneracion) { this.fechaHoraGeneracion = fechaHoraGeneracion; }
    public Integer getTotalRegistrosDeclarado() { return totalRegistrosDeclarado; }
    public void setTotalRegistrosDeclarado(Integer totalRegistrosDeclarado) { this.totalRegistrosDeclarado = totalRegistrosDeclarado; }
    public BigDecimal getMontoTotalDeclarado() { return montoTotalDeclarado; }
    public void setMontoTotalDeclarado(BigDecimal montoTotalDeclarado) { this.montoTotalDeclarado = montoTotalDeclarado; }
    public Integer getTotalRegistrosPie() { return totalRegistrosPie; }
    public void setTotalRegistrosPie(Integer totalRegistrosPie) { this.totalRegistrosPie = totalRegistrosPie; }
    public BigDecimal getMontoTotalPie() { return montoTotalPie; }
    public void setMontoTotalPie(BigDecimal montoTotalPie) { this.montoTotalPie = montoTotalPie; }
    public Integer getTotalRegistrosValidados() { return totalRegistrosValidados; }
    public void setTotalRegistrosValidados(Integer totalRegistrosValidados) { this.totalRegistrosValidados = totalRegistrosValidados; }
    public Integer getTotalRegistrosRechazados() { return totalRegistrosRechazados; }
    public void setTotalRegistrosRechazados(Integer totalRegistrosRechazados) { this.totalRegistrosRechazados = totalRegistrosRechazados; }
    public BigDecimal getMontoTotalValidado() { return montoTotalValidado; }
    public void setMontoTotalValidado(BigDecimal montoTotalValidado) { this.montoTotalValidado = montoTotalValidado; }
    public String getNombreArchivo() { return nombreArchivo; }
    public void setNombreArchivo(String nombreArchivo) { this.nombreArchivo = nombreArchivo; }
    public String getHashArchivo() { return hashArchivo; }
    public void setHashArchivo(String hashArchivo) { this.hashArchivo = hashArchivo; }
    public String getHashPieControl() { return hashPieControl; }
    public void setHashPieControl(String hashPieControl) { this.hashPieControl = hashPieControl; }
    public Long getTamanoBytes() { return tamanoBytes; }
    public void setTamanoBytes(Long tamanoBytes) { this.tamanoBytes = tamanoBytes; }
    public FormatoArchivo getFormatoArchivo() { return formatoArchivo; }
    public void setFormatoArchivo(FormatoArchivo formatoArchivo) { this.formatoArchivo = formatoArchivo; }
    public String getRutaAlmacenamiento() { return rutaAlmacenamiento; }
    public void setRutaAlmacenamiento(String rutaAlmacenamiento) { this.rutaAlmacenamiento = rutaAlmacenamiento; }
    public CanalIngreso getCanalIngreso() { return canalIngreso; }
    public void setCanalIngreso(CanalIngreso canalIngreso) { this.canalIngreso = canalIngreso; }
    public EstadoLote getEstado() { return estado; }
    public void setEstado(EstadoLote estado) { this.estado = estado; }
    public String getMotivoRechazoGlobal() { return motivoRechazoGlobal; }
    public void setMotivoRechazoGlobal(String motivoRechazoGlobal) { this.motivoRechazoGlobal = motivoRechazoGlobal; }
    public OffsetDateTime getFechaRecepcion() { return fechaRecepcion; }
    public void setFechaRecepcion(OffsetDateTime fechaRecepcion) { this.fechaRecepcion = fechaRecepcion; }
    public OffsetDateTime getFechaInicioValidacion() { return fechaInicioValidacion; }
    public void setFechaInicioValidacion(OffsetDateTime fechaInicioValidacion) { this.fechaInicioValidacion = fechaInicioValidacion; }
    public OffsetDateTime getFechaFinValidacion() { return fechaFinValidacion; }
    public void setFechaFinValidacion(OffsetDateTime fechaFinValidacion) { this.fechaFinValidacion = fechaFinValidacion; }
    public OffsetDateTime getFechaInicioProceso() { return fechaInicioProceso; }
    public void setFechaInicioProceso(OffsetDateTime fechaInicioProceso) { this.fechaInicioProceso = fechaInicioProceso; }
    public OffsetDateTime getFechaFinProceso() { return fechaFinProceso; }
    public void setFechaFinProceso(OffsetDateTime fechaFinProceso) { this.fechaFinProceso = fechaFinProceso; }
    public OffsetDateTime getFechaCierre() { return fechaCierre; }
    public void setFechaCierre(OffsetDateTime fechaCierre) { this.fechaCierre = fechaCierre; }
    public OffsetDateTime getFechaActualizacion() { return fechaActualizacion; }
    public void setFechaActualizacion(OffsetDateTime fechaActualizacion) { this.fechaActualizacion = fechaActualizacion; }
    public Integer getVersion() { return version; }
    public void setVersion(Integer version) { this.version = version; }

    @Override
    public boolean equals(Object objeto) {
        if (this == objeto) {
            return true;
        }
        if (!(objeto instanceof LotePago lotePago)) {
            return false;
        }
        return idLote != null && Objects.equals(idLote, lotePago.idLote);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idLote);
    }

    @Override
    public String toString() {
        return "LotePago{" +
                "idLote=" + idLote +
                ", uuidLote=" + uuidLote +
                ", rucEmpresa='" + rucEmpresa + '\'' +
                ", nombreArchivo='" + nombreArchivo + '\'' +
                ", estado=" + estado +
                '}';
    }
}
