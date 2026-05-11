package com.banquito.switchpagos.lote.model;

import com.banquito.switchpagos.catalogo.model.TipoServicio;
import com.banquito.switchpagos.lote.enums.CanalIngreso;
import com.banquito.switchpagos.lote.enums.EstadoLote;
import com.banquito.switchpagos.lote.enums.FormatoArchivo;
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
    @Column(name = "ID_LOTE")
    private Long idLote;
    @Column(name = "UUID_LOTE")
    private UUID uuidLote;
    @Column(name = "CLAVE_IDEMPOTENCIA")
    private UUID claveIdempotencia;
    @Column(name = "RUC_EMPRESA")
    private String rucEmpresa;
    @Column(name = "ID_CREDENCIAL_WEB_CORE")
    private Integer idCredencialWebCore;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "TIPO_SERVICIO")
    private TipoServicio tipoServicio;
    @Column(name = "CUENTA_MATRIZ_CARGO")
    private String cuentaMatrizCargo;
    @Column(name = "FECHA_HORA_GENERACION")
    private OffsetDateTime fechaHoraGeneracion;
    @Column(name = "TOTAL_REGISTROS_DECLARADO")
    private Integer totalRegistrosDeclarado;
    @Column(name = "MONTO_TOTAL_DECLARADO")
    private BigDecimal montoTotalDeclarado;
    @Column(name = "TOTAL_REGISTROS_PIE")
    private Integer totalRegistrosPie;
    @Column(name = "MONTO_TOTAL_PIE")
    private BigDecimal montoTotalPie;
    @Column(name = "TOTAL_REGISTROS_VALIDADOS")
    private Integer totalRegistrosValidados;
    @Column(name = "TOTAL_REGISTROS_RECHAZADOS")
    private Integer totalRegistrosRechazados;
    @Column(name = "MONTO_TOTAL_VALIDADO")
    private BigDecimal montoTotalValidado;
    @Column(name = "NOMBRE_ARCHIVO")
    private String nombreArchivo;
    @Column(name = "HASH_ARCHIVO")
    private String hashArchivo;
    @Column(name = "HASH_PIE_CONTROL")
    private String hashPieControl;
    @Column(name = "TAMANO_BYTES")
    private Long tamanoBytes;
    @Enumerated(EnumType.STRING)
    @Column(name = "FORMATO_ARCHIVO")
    private FormatoArchivo formatoArchivo;
    @Column(name = "RUTA_ALMACENAMIENTO")
    private String rutaAlmacenamiento;
    @Enumerated(EnumType.STRING)
    @Column(name = "CANAL_INGRESO")
    private CanalIngreso canalIngreso;
    @Enumerated(EnumType.STRING)
    @Column(name = "ESTADO")
    private EstadoLote estado;
    @Column(name = "MOTIVO_RECHAZO_GLOBAL")
    private String motivoRechazoGlobal;
    @Column(name = "FECHA_RECEPCION")
    private OffsetDateTime fechaRecepcion;
    @Column(name = "FECHA_INICIO_VALIDACION")
    private OffsetDateTime fechaInicioValidacion;
    @Column(name = "FECHA_FIN_VALIDACION")
    private OffsetDateTime fechaFinValidacion;
    @Column(name = "FECHA_INICIO_PROCESO")
    private OffsetDateTime fechaInicioProceso;
    @Column(name = "FECHA_FIN_PROCESO")
    private OffsetDateTime fechaFinProceso;
    @Column(name = "FECHA_CIERRE")
    private OffsetDateTime fechaCierre;
    @Column(name = "FECHA_ACTUALIZACION")
    private OffsetDateTime fechaActualizacion;
    @Version
    @Column(name = "VERSION")
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
