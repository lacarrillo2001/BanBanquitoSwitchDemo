package com.banquito.switchpagos.batch.model;

import com.banquito.switchpagos.batch.enums.EstadoColaProcesamiento;
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

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Objects;

@Entity
@Table(schema = "switch_banquito", name = "COLA_PROCESAMIENTO")
public class ColaProcesamiento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_cola")
    private Long idCola;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_lote", referencedColumnName = "id_lote")
    private LotePago lotePago;
    @Column(name = "fecha_habil_programada")
    private LocalDate fechaHabilProgramada;
    @Column(name = "fecha_encolado")
    private OffsetDateTime fechaEncolado;
    @Column(name = "fecha_programada_proceso")
    private OffsetDateTime fechaProgramadaProceso;
    @Enumerated(EnumType.STRING)
    @Column(name = "estado_cola")
    private EstadoColaProcesamiento estadoCola;
    @Column(name = "prioridad")
    private Integer prioridad;
    @Column(name = "intentos")
    private Integer intentos;
    @Column(name = "max_intentos")
    private Integer maxIntentos;
    @Column(name = "tomado_por")
    private String tomadoPor;
    @Column(name = "tomado_en")
    private OffsetDateTime tomadoEn;
    @Column(name = "proximo_reintento_en")
    private OffsetDateTime proximoReintentoEn;
    @Column(name = "ultimo_error")
    private String ultimoError;
    @Column(name = "fecha_actualizacion")
    private OffsetDateTime fechaActualizacion;
    @Version
    @Column(name = "version")
    private Integer version;

    public ColaProcesamiento() {
    }

    public ColaProcesamiento(Long idCola) {
        this.idCola = idCola;
    }

    public Long getIdCola() { return idCola; }
    public void setIdCola(Long idCola) { this.idCola = idCola; }
    public LotePago getLotePago() { return lotePago; }
    public void setLotePago(LotePago lotePago) { this.lotePago = lotePago; }
    public LocalDate getFechaHabilProgramada() { return fechaHabilProgramada; }
    public void setFechaHabilProgramada(LocalDate fechaHabilProgramada) { this.fechaHabilProgramada = fechaHabilProgramada; }
    public OffsetDateTime getFechaEncolado() { return fechaEncolado; }
    public void setFechaEncolado(OffsetDateTime fechaEncolado) { this.fechaEncolado = fechaEncolado; }
    public OffsetDateTime getFechaProgramadaProceso() { return fechaProgramadaProceso; }
    public void setFechaProgramadaProceso(OffsetDateTime fechaProgramadaProceso) { this.fechaProgramadaProceso = fechaProgramadaProceso; }
    public EstadoColaProcesamiento getEstadoCola() { return estadoCola; }
    public void setEstadoCola(EstadoColaProcesamiento estadoCola) { this.estadoCola = estadoCola; }
    public Integer getPrioridad() { return prioridad; }
    public void setPrioridad(Integer prioridad) { this.prioridad = prioridad; }
    public Integer getIntentos() { return intentos; }
    public void setIntentos(Integer intentos) { this.intentos = intentos; }
    public Integer getMaxIntentos() { return maxIntentos; }
    public void setMaxIntentos(Integer maxIntentos) { this.maxIntentos = maxIntentos; }
    public String getTomadoPor() { return tomadoPor; }
    public void setTomadoPor(String tomadoPor) { this.tomadoPor = tomadoPor; }
    public OffsetDateTime getTomadoEn() { return tomadoEn; }
    public void setTomadoEn(OffsetDateTime tomadoEn) { this.tomadoEn = tomadoEn; }
    public OffsetDateTime getProximoReintentoEn() { return proximoReintentoEn; }
    public void setProximoReintentoEn(OffsetDateTime proximoReintentoEn) { this.proximoReintentoEn = proximoReintentoEn; }
    public String getUltimoError() { return ultimoError; }
    public void setUltimoError(String ultimoError) { this.ultimoError = ultimoError; }
    public OffsetDateTime getFechaActualizacion() { return fechaActualizacion; }
    public void setFechaActualizacion(OffsetDateTime fechaActualizacion) { this.fechaActualizacion = fechaActualizacion; }
    public Integer getVersion() { return version; }
    public void setVersion(Integer version) { this.version = version; }

    @Override
    public boolean equals(Object objeto) {
        if (this == objeto) {
            return true;
        }
        if (!(objeto instanceof ColaProcesamiento colaProcesamiento)) {
            return false;
        }
        return idCola != null && Objects.equals(idCola, colaProcesamiento.idCola);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idCola);
    }

    @Override
    public String toString() {
        return "ColaProcesamiento{" +
                "idCola=" + idCola +
                ", fechaProgramadaProceso=" + fechaProgramadaProceso +
                ", estadoCola=" + estadoCola +
                ", prioridad=" + prioridad +
                '}';
    }
}
