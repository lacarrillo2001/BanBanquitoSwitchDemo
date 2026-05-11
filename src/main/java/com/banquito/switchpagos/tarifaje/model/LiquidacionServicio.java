package com.banquito.switchpagos.tarifaje.model;

import com.banquito.switchpagos.lote.model.LotePago;
import com.banquito.switchpagos.tarifaje.enums.EstadoDebitoLiquidacion;
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

@Entity
@Table(schema = "switch_banquito", name = "LIQUIDACION_SERVICIO")
public class LiquidacionServicio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_LIQUIDACION")
    private Long idLiquidacion;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_LOTE")
    private LotePago lotePago;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_TARIFA_APLICADA")
    private TarifaServicio tarifaAplicada;
    @Column(name = "TRANSACCIONES_EXITOSAS")
    private Integer transaccionesExitosas;
    @Column(name = "TRANSACCIONES_FALLIDAS")
    private Integer transaccionesFallidas;
    @Column(name = "TARIFA_UNITARIA_APLICADA")
    private BigDecimal tarifaUnitariaAplicada;
    @Column(name = "IVA_PORCENTAJE_APLICADO")
    private BigDecimal ivaPorcentajeAplicado;
    @Column(name = "SUBTOTAL_COMISION")
    private BigDecimal subtotalComision;
    @Column(name = "MONTO_IVA")
    private BigDecimal montoIva;
    @Column(name = "TOTAL_DEBITADO")
    private BigDecimal totalDebitado;
    @Enumerated(EnumType.STRING)
    @Column(name = "ESTADO_DEBITO")
    private EstadoDebitoLiquidacion estadoDebito;
    @Column(name = "PERMITE_SOBREGIRO")
    private Boolean permiteSobregiro;
    @Column(name = "FECHA_LIQUIDACION")
    private OffsetDateTime fechaLiquidacion;
    @Column(name = "FECHA_CREACION")
    private OffsetDateTime fechaCreacion;
    @Column(name = "FECHA_ACTUALIZACION")
    private OffsetDateTime fechaActualizacion;
    @Version
    @Column(name = "VERSION")
    private Integer version;

    public LiquidacionServicio() {
    }

    public LiquidacionServicio(Long idLiquidacion) {
        this.idLiquidacion = idLiquidacion;
    }

    public Long getIdLiquidacion() { return idLiquidacion; }
    public void setIdLiquidacion(Long idLiquidacion) { this.idLiquidacion = idLiquidacion; }
    public LotePago getLotePago() { return lotePago; }
    public void setLotePago(LotePago lotePago) { this.lotePago = lotePago; }
    public TarifaServicio getTarifaAplicada() { return tarifaAplicada; }
    public void setTarifaAplicada(TarifaServicio tarifaAplicada) { this.tarifaAplicada = tarifaAplicada; }
    public Integer getTransaccionesExitosas() { return transaccionesExitosas; }
    public void setTransaccionesExitosas(Integer transaccionesExitosas) { this.transaccionesExitosas = transaccionesExitosas; }
    public Integer getTransaccionesFallidas() { return transaccionesFallidas; }
    public void setTransaccionesFallidas(Integer transaccionesFallidas) { this.transaccionesFallidas = transaccionesFallidas; }
    public BigDecimal getTarifaUnitariaAplicada() { return tarifaUnitariaAplicada; }
    public void setTarifaUnitariaAplicada(BigDecimal tarifaUnitariaAplicada) { this.tarifaUnitariaAplicada = tarifaUnitariaAplicada; }
    public BigDecimal getIvaPorcentajeAplicado() { return ivaPorcentajeAplicado; }
    public void setIvaPorcentajeAplicado(BigDecimal ivaPorcentajeAplicado) { this.ivaPorcentajeAplicado = ivaPorcentajeAplicado; }
    public BigDecimal getSubtotalComision() { return subtotalComision; }
    public void setSubtotalComision(BigDecimal subtotalComision) { this.subtotalComision = subtotalComision; }
    public BigDecimal getMontoIva() { return montoIva; }
    public void setMontoIva(BigDecimal montoIva) { this.montoIva = montoIva; }
    public BigDecimal getTotalDebitado() { return totalDebitado; }
    public void setTotalDebitado(BigDecimal totalDebitado) { this.totalDebitado = totalDebitado; }
    public EstadoDebitoLiquidacion getEstadoDebito() { return estadoDebito; }
    public void setEstadoDebito(EstadoDebitoLiquidacion estadoDebito) { this.estadoDebito = estadoDebito; }
    public Boolean getPermiteSobregiro() { return permiteSobregiro; }
    public void setPermiteSobregiro(Boolean permiteSobregiro) { this.permiteSobregiro = permiteSobregiro; }
    public OffsetDateTime getFechaLiquidacion() { return fechaLiquidacion; }
    public void setFechaLiquidacion(OffsetDateTime fechaLiquidacion) { this.fechaLiquidacion = fechaLiquidacion; }
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
        if (!(objeto instanceof LiquidacionServicio liquidacionServicio)) {
            return false;
        }
        return idLiquidacion != null && Objects.equals(idLiquidacion, liquidacionServicio.idLiquidacion);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idLiquidacion);
    }

    @Override
    public String toString() {
        return "LiquidacionServicio{" +
                "idLiquidacion=" + idLiquidacion +
                ", transaccionesExitosas=" + transaccionesExitosas +
                ", totalDebitado=" + totalDebitado +
                ", estadoDebito=" + estadoDebito +
                '}';
    }
}
