package com.banquito.switchpagos.processing.model;

import com.banquito.switchpagos.catalog.model.TipoServicio;
import com.banquito.switchpagos.processing.enums.EstadoLimiteTransaccion;
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
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Objects;

@Entity
@Table(schema = "switch_banquito", name = "LIMITE_TRANSACCION")
public class LimiteTransaccion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_limite")
    private Integer idLimite;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tipo_servicio", referencedColumnName = "codigo")
    private TipoServicio tipoServicio;
    @Column(name = "monto_minimo")
    private BigDecimal montoMinimo;
    @Column(name = "monto_maximo")
    private BigDecimal montoMaximo;
    @Column(name = "moneda")
    private String moneda;
    @Column(name = "vigente_desde")
    private LocalDate vigenteDesde;
    @Column(name = "vigente_hasta")
    private LocalDate vigenteHasta;
    @Enumerated(EnumType.STRING)
    @Column(name = "estado")
    private EstadoLimiteTransaccion estado;
    @Column(name = "fecha_creacion")
    private OffsetDateTime fechaCreacion;
    @Column(name = "fecha_actualizacion")
    private OffsetDateTime fechaActualizacion;
    @Version
    @Column(name = "version")
    private Integer version;

    public LimiteTransaccion() {
    }

    public LimiteTransaccion(Integer idLimite) {
        this.idLimite = idLimite;
    }

    public Integer getIdLimite() { return idLimite; }
    public void setIdLimite(Integer idLimite) { this.idLimite = idLimite; }
    public TipoServicio getTipoServicio() { return tipoServicio; }
    public void setTipoServicio(TipoServicio tipoServicio) { this.tipoServicio = tipoServicio; }
    public BigDecimal getMontoMinimo() { return montoMinimo; }
    public void setMontoMinimo(BigDecimal montoMinimo) { this.montoMinimo = montoMinimo; }
    public BigDecimal getMontoMaximo() { return montoMaximo; }
    public void setMontoMaximo(BigDecimal montoMaximo) { this.montoMaximo = montoMaximo; }
    public String getMoneda() { return moneda; }
    public void setMoneda(String moneda) { this.moneda = moneda; }
    public LocalDate getVigenteDesde() { return vigenteDesde; }
    public void setVigenteDesde(LocalDate vigenteDesde) { this.vigenteDesde = vigenteDesde; }
    public LocalDate getVigenteHasta() { return vigenteHasta; }
    public void setVigenteHasta(LocalDate vigenteHasta) { this.vigenteHasta = vigenteHasta; }
    public EstadoLimiteTransaccion getEstado() { return estado; }
    public void setEstado(EstadoLimiteTransaccion estado) { this.estado = estado; }
    public OffsetDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(OffsetDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }
    public OffsetDateTime getFechaActualizacion() { return fechaActualizacion; }
    public void setFechaActualizacion(OffsetDateTime fechaActualizacion) { this.fechaActualizacion = fechaActualizacion; }
    public Integer getVersion() { return version; }
    public void setVersion(Integer version) { this.version = version; }

    @Override
    public boolean equals(Object objeto) {
        if (this == objeto) {
            return true;
        }
        if (!(objeto instanceof LimiteTransaccion limiteTransaccion)) {
            return false;
        }
        return idLimite != null && Objects.equals(idLimite, limiteTransaccion.idLimite);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idLimite);
    }

    @Override
    public String toString() {
        return "LimiteTransaccion{" +
                "idLimite=" + idLimite +
                ", montoMinimo=" + montoMinimo +
                ", montoMaximo=" + montoMaximo +
                ", estado=" + estado +
                '}';
    }
}
