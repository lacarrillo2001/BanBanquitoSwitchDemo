package com.banquito.switchpagos.tarifaje.model;

import com.banquito.switchpagos.catalogo.model.TipoServicio;
import com.banquito.switchpagos.tarifaje.enums.EstadoTarifaServicio;
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
    @Column(name = "ID_TARIFA")
    private Integer idTarifa;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "TIPO_SERVICIO")
    private TipoServicio tipoServicio;
    @Column(name = "RANGO_DESDE")
    private Integer rangoDesde;
    @Column(name = "RANGO_HASTA")
    private Integer rangoHasta;
    @Column(name = "TARIFA_UNITARIA")
    private BigDecimal tarifaUnitaria;
    @Column(name = "MONEDA")
    private String moneda;
    @Column(name = "VIGENTE_DESDE")
    private LocalDate vigenteDesde;
    @Column(name = "VIGENTE_HASTA")
    private LocalDate vigenteHasta;
    @Enumerated(EnumType.STRING)
    @Column(name = "ESTADO")
    private EstadoTarifaServicio estado;
    @Column(name = "FECHA_CREACION")
    private OffsetDateTime fechaCreacion;
    @Column(name = "FECHA_ACTUALIZACION")
    private OffsetDateTime fechaActualizacion;
    @Version
    @Column(name = "VERSION")
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
