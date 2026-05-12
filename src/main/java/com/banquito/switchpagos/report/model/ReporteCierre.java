package com.banquito.switchpagos.report.model;

import com.banquito.switchpagos.batch.model.LotePago;
import com.banquito.switchpagos.report.enums.FormatoReporte;
import com.banquito.switchpagos.report.enums.TipoReporte;
import com.fasterxml.jackson.databind.JsonNode;
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
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.Objects;

@Entity
@Table(schema = "switch_banquito", name = "REPORTE_CIERRE")
public class ReporteCierre {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_reporte")
    private Long idReporte;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_lote", referencedColumnName = "id_lote")
    private LotePago lotePago;
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_reporte")
    private TipoReporte tipoReporte;
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "contenido_json")
    private JsonNode contenidoJson;
    @Column(name = "nombre_archivo")
    private String nombreArchivo;
    @Enumerated(EnumType.STRING)
    @Column(name = "formato_archivo")
    private FormatoReporte formatoArchivo;
    @Column(name = "url_archivo")
    private String urlArchivo;
    @Column(name = "hash_reporte")
    private String hashReporte;
    @Column(name = "fecha_generacion")
    private OffsetDateTime fechaGeneracion;
    @Column(name = "descargado_empresa")
    private Boolean descargadoEmpresa;
    @Column(name = "fecha_descarga")
    private OffsetDateTime fechaDescarga;
    @Column(name = "fecha_actualizacion")
    private OffsetDateTime fechaActualizacion;
    @Version
    @Column(name = "version")
    private Integer version;

    public ReporteCierre() {
    }

    public ReporteCierre(Long idReporte) {
        this.idReporte = idReporte;
    }

    public Long getIdReporte() { return idReporte; }
    public void setIdReporte(Long idReporte) { this.idReporte = idReporte; }
    public LotePago getLotePago() { return lotePago; }
    public void setLotePago(LotePago lotePago) { this.lotePago = lotePago; }
    public TipoReporte getTipoReporte() { return tipoReporte; }
    public void setTipoReporte(TipoReporte tipoReporte) { this.tipoReporte = tipoReporte; }
    public JsonNode getContenidoJson() { return contenidoJson; }
    public void setContenidoJson(JsonNode contenidoJson) { this.contenidoJson = contenidoJson; }
    public String getNombreArchivo() { return nombreArchivo; }
    public void setNombreArchivo(String nombreArchivo) { this.nombreArchivo = nombreArchivo; }
    public FormatoReporte getFormatoArchivo() { return formatoArchivo; }
    public void setFormatoArchivo(FormatoReporte formatoArchivo) { this.formatoArchivo = formatoArchivo; }
    public String getUrlArchivo() { return urlArchivo; }
    public void setUrlArchivo(String urlArchivo) { this.urlArchivo = urlArchivo; }
    public String getHashReporte() { return hashReporte; }
    public void setHashReporte(String hashReporte) { this.hashReporte = hashReporte; }
    public OffsetDateTime getFechaGeneracion() { return fechaGeneracion; }
    public void setFechaGeneracion(OffsetDateTime fechaGeneracion) { this.fechaGeneracion = fechaGeneracion; }
    public Boolean getDescargadoEmpresa() { return descargadoEmpresa; }
    public void setDescargadoEmpresa(Boolean descargadoEmpresa) { this.descargadoEmpresa = descargadoEmpresa; }
    public OffsetDateTime getFechaDescarga() { return fechaDescarga; }
    public void setFechaDescarga(OffsetDateTime fechaDescarga) { this.fechaDescarga = fechaDescarga; }
    public OffsetDateTime getFechaActualizacion() { return fechaActualizacion; }
    public void setFechaActualizacion(OffsetDateTime fechaActualizacion) { this.fechaActualizacion = fechaActualizacion; }
    public Integer getVersion() { return version; }
    public void setVersion(Integer version) { this.version = version; }

    @Override
    public boolean equals(Object objeto) {
        if (this == objeto) {
            return true;
        }
        if (!(objeto instanceof ReporteCierre reporteCierre)) {
            return false;
        }
        return idReporte != null && Objects.equals(idReporte, reporteCierre.idReporte);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idReporte);
    }

    @Override
    public String toString() {
        return "ReporteCierre{" +
                "idReporte=" + idReporte +
                ", tipoReporte=" + tipoReporte +
                ", nombreArchivo='" + nombreArchivo + '\'' +
                ", formatoArchivo=" + formatoArchivo +
                '}';
    }
}
