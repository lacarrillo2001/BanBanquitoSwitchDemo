package com.banquito.switchpagos.pricing.model;

import com.banquito.switchpagos.pricing.enums.ConceptoDetalleLiquidacion;
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

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(schema = "switch_banquito", name = "DETALLE_LIQUIDACION")
public class DetalleLiquidacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_detalle")
    private Long idDetalle;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_liquidacion", referencedColumnName = "id_liquidacion")
    private LiquidacionServicio liquidacionServicio;
    @Enumerated(EnumType.STRING)
    @Column(name = "concepto")
    private ConceptoDetalleLiquidacion concepto;
    @Column(name = "monto")
    private BigDecimal monto;
    @Column(name = "uuid_transaccion_core")
    private UUID uuidTransaccionCore;
    @Column(name = "cuenta_origen_core")
    private String cuentaOrigenCore;
    @Column(name = "cuenta_destino_core")
    private String cuentaDestinoCore;
    @Column(name = "fecha_creacion")
    private OffsetDateTime fechaCreacion;

    public DetalleLiquidacion() {
    }

    public DetalleLiquidacion(Long idDetalle) {
        this.idDetalle = idDetalle;
    }

    public Long getIdDetalle() { return idDetalle; }
    public void setIdDetalle(Long idDetalle) { this.idDetalle = idDetalle; }
    public LiquidacionServicio getLiquidacionServicio() { return liquidacionServicio; }
    public void setLiquidacionServicio(LiquidacionServicio liquidacionServicio) { this.liquidacionServicio = liquidacionServicio; }
    public ConceptoDetalleLiquidacion getConcepto() { return concepto; }
    public void setConcepto(ConceptoDetalleLiquidacion concepto) { this.concepto = concepto; }
    public BigDecimal getMonto() { return monto; }
    public void setMonto(BigDecimal monto) { this.monto = monto; }
    public UUID getUuidTransaccionCore() { return uuidTransaccionCore; }
    public void setUuidTransaccionCore(UUID uuidTransaccionCore) { this.uuidTransaccionCore = uuidTransaccionCore; }
    public String getCuentaOrigenCore() { return cuentaOrigenCore; }
    public void setCuentaOrigenCore(String cuentaOrigenCore) { this.cuentaOrigenCore = cuentaOrigenCore; }
    public String getCuentaDestinoCore() { return cuentaDestinoCore; }
    public void setCuentaDestinoCore(String cuentaDestinoCore) { this.cuentaDestinoCore = cuentaDestinoCore; }
    public OffsetDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(OffsetDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    @Override
    public boolean equals(Object objeto) {
        if (this == objeto) {
            return true;
        }
        if (!(objeto instanceof DetalleLiquidacion detalleLiquidacion)) {
            return false;
        }
        return idDetalle != null && Objects.equals(idDetalle, detalleLiquidacion.idDetalle);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idDetalle);
    }

    @Override
    public String toString() {
        return "DetalleLiquidacion{" +
                "idDetalle=" + idDetalle +
                ", concepto=" + concepto +
                ", monto=" + monto +
                ", uuidTransaccionCore=" + uuidTransaccionCore +
                '}';
    }
}
