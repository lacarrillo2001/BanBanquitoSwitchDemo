package com.banquito.switchpagos.pricing.model;

import com.banquito.switchpagos.catalog.model.TipoServicio;
import com.banquito.switchpagos.pricing.enums.EstadoTarifaServicio;
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
@Table(schema = "switch_banquito", name = "TARIFA_SERVICIO")
public class TarifaServicio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_tarifa")
    private Integer idTarifa;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tipo_servicio", referencedColumnName = "codigo")
    private TipoServicio tipoServicio;
    @Column(name = "rango_desde")
    private Integer rangoDesde;
    @Column(name = "rango_hasta")
    private Integer rangoHasta;
    @Column(name = "tarifa_unitaria")
    private BigDecimal tarifaUnitaria;
    @Column(name = "moneda")
    private String moneda;
    @Column(name = "vigente_desde")
    private LocalDate vigenteDesde;
    @Column(name = "vigente_hasta")
    private LocalDate vigenteHasta;
    @Enumerated(EnumType.STRING)
    @Column(name = "estado")
    private EstadoTarifaServicio estado;
    @Column(name = "fecha_creacion")
    private OffsetDateTime fechaCreacion;
    @Column(name = "fecha_actualizacion")
    private OffsetDateTime fechaActualizacion;
    @Version
    @Column(name = "version")
    private Integer version;

    public TarifaServicio() {
    }

    public TarifaServicio(Integer idTarifa) {
        this.idTarifa = idTarifa;
    }

    public Integer getIdTarifa() { return idTarifa; }
    public void setIdTarifa(Integer idTarifa) { this.idTarifa = idTarifa; }
    public TipoServicio getTipoServicio() { return tipoServicio; }
    public void setTipoServicio(TipoServicio tipoServicio) { this.tipoServicio = tipoServicio; }
    public Integer getRangoDesde() { return rangoDesde; }
    public void setRangoDesde(Integer rangoDesde) { this.rangoDesde = rangoDesde; }
    public Integer getRangoHasta() { return rangoHasta; }
    public void setRangoHasta(Integer rangoHasta) { this.rangoHasta = rangoHasta; }
    public BigDecimal getTarifaUnitaria() { return tarifaUnitaria; }
    public void setTarifaUnitaria(BigDecimal tarifaUnitaria) { this.tarifaUnitaria = tarifaUnitaria; }
    public String getMoneda() { return moneda; }
    public void setMoneda(String moneda) { this.moneda = moneda; }
    public LocalDate getVigenteDesde() { return vigenteDesde; }
    public void setVigenteDesde(LocalDate vigenteDesde) { this.vigenteDesde = vigenteDesde; }
    public LocalDate getVigenteHasta() { return vigenteHasta; }
    public void setVigenteHasta(LocalDate vigenteHasta) { this.vigenteHasta = vigenteHasta; }
    public EstadoTarifaServicio getEstado() { return estado; }
    public void setEstado(EstadoTarifaServicio estado) { this.estado = estado; }
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
        if (!(objeto instanceof TarifaServicio tarifaServicio)) {
            return false;
        }
        return idTarifa != null && Objects.equals(idTarifa, tarifaServicio.idTarifa);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idTarifa);
    }

    @Override
    public String toString() {
        return "TarifaServicio{" +
                "idTarifa=" + idTarifa +
                ", rangoDesde=" + rangoDesde +
                ", rangoHasta=" + rangoHasta +
                ", tarifaUnitaria=" + tarifaUnitaria +
                ", estado=" + estado +
                '}';
    }
}
