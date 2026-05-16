package com.banquito.switchpagos.batch.model;

import com.banquito.switchpagos.batch.enums.EstadoLote;
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

import java.time.OffsetDateTime;
import java.util.Objects;

@Entity
@Table(schema = "switch_banquito", name = "HISTORIAL_ESTADO_LOTE")
public class HistorialEstadoLote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_historial")
    private Long idHistorial;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_lote", referencedColumnName = "id_lote")
    private LotePago lotePago;
    @Enumerated(EnumType.STRING)
    @Column(name = "estado_anterior")
    private EstadoLote estadoAnterior;
    @Enumerated(EnumType.STRING)
    @Column(name = "estado_nuevo")
    private EstadoLote estadoNuevo;
    @Column(name = "motivo")
    private String motivo;
    @Column(name = "cambiado_por")
    private String cambiadoPor;
    @Column(name = "fecha_cambio")
    private OffsetDateTime fechaCambio;

    public HistorialEstadoLote() {
    }

    public HistorialEstadoLote(Long idHistorial) {
        this.idHistorial = idHistorial;
    }

    public Long getIdHistorial() { return idHistorial; }
    public void setIdHistorial(Long idHistorial) { this.idHistorial = idHistorial; }
    public LotePago getLotePago() { return lotePago; }
    public void setLotePago(LotePago lotePago) { this.lotePago = lotePago; }
    public EstadoLote getEstadoAnterior() { return estadoAnterior; }
    public void setEstadoAnterior(EstadoLote estadoAnterior) { this.estadoAnterior = estadoAnterior; }
    public EstadoLote getEstadoNuevo() { return estadoNuevo; }
    public void setEstadoNuevo(EstadoLote estadoNuevo) { this.estadoNuevo = estadoNuevo; }
    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }
    public String getCambiadoPor() { return cambiadoPor; }
    public void setCambiadoPor(String cambiadoPor) { this.cambiadoPor = cambiadoPor; }
    public OffsetDateTime getFechaCambio() { return fechaCambio; }
    public void setFechaCambio(OffsetDateTime fechaCambio) { this.fechaCambio = fechaCambio; }

    @Override
    public boolean equals(Object objeto) {
        if (this == objeto) {
            return true;
        }
        if (!(objeto instanceof HistorialEstadoLote historialEstadoLote)) {
            return false;
        }
        return idHistorial != null && Objects.equals(idHistorial, historialEstadoLote.idHistorial);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idHistorial);
    }

    @Override
    public String toString() {
        return "HistorialEstadoLote{" +
                "idHistorial=" + idHistorial +
                ", estadoAnterior=" + estadoAnterior +
                ", estadoNuevo=" + estadoNuevo +
                ", fechaCambio=" + fechaCambio +
                '}';
    }
}
