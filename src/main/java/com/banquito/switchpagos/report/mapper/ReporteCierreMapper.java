package com.banquito.switchpagos.report.mapper;

import com.banquito.switchpagos.batch.dto.internal.LoteProcesamientoInternalDto;
import com.banquito.switchpagos.batch.model.LotePago;
import com.banquito.switchpagos.processing.dto.internal.LineaPagoInternalDto;
import com.banquito.switchpagos.report.dto.api.ComprobanteLiquidacionResponse;
import com.banquito.switchpagos.report.dto.api.EmpresaComprobanteResponse;
import com.banquito.switchpagos.report.dto.api.LineaNovedadResponse;
import com.banquito.switchpagos.report.dto.api.LiquidacionComprobanteResponse;
import com.banquito.switchpagos.report.dto.api.ReporteNovedadesResponse;
import com.banquito.switchpagos.report.dto.api.ResumenNovedadesResponse;
import com.banquito.switchpagos.report.dto.api.ResumenPagosComprobanteResponse;
import com.banquito.switchpagos.report.enums.FormatoReporte;
import com.banquito.switchpagos.report.enums.TipoReporte;
import com.banquito.switchpagos.report.model.ReporteCierre;
import com.banquito.switchpagos.pricing.dto.internal.LiquidacionComprobanteInternalDto;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Component
public class ReporteCierreMapper {

    public ReporteNovedadesResponse toNovedadesResponse(UUID uuidLote, FormatoReporte formato,
                                                        ResumenNovedadesResponse resumen,
                                                        List<LineaPagoInternalDto> lineas,
                                                        OffsetDateTime fechaGeneracion) {
        return new ReporteNovedadesResponse(
                uuidLote,
                TipoReporte.REPORTE_NOVEDADES.name(),
                formato.name(),
                fechaGeneracion,
                resumen,
                lineas.stream().map(this::toLineaNovedadResponse).toList()
        );
    }

    public ComprobanteLiquidacionResponse toComprobanteResponse(UUID uuidLote, FormatoReporte formato,
                                                                LoteProcesamientoInternalDto lote,
                                                                LiquidacionComprobanteInternalDto liquidacion,
                                                                BigDecimal montoDispersado,
                                                                OffsetDateTime fechaGeneracion) {
        return new ComprobanteLiquidacionResponse(
                uuidLote,
                TipoReporte.COMPROBANTE_LIQUIDACION.name(),
                formato.name(),
                new EmpresaComprobanteResponse(lote.rucEmpresa(), lote.cuentaMatrizCargo()),
                new ResumenPagosComprobanteResponse(
                        liquidacion.transaccionesExitosas(),
                        liquidacion.transaccionesFallidas(),
                        montoDispersado
                ),
                new LiquidacionComprobanteResponse(
                        liquidacion.tarifaUnitariaAplicada(),
                        liquidacion.subtotalComision(),
                        liquidacion.ivaPorcentajeAplicado(),
                        liquidacion.montoIva(),
                        liquidacion.totalDebitado()
                ),
                fechaGeneracion
        );
    }

    public ReporteCierre toEntity(LotePago lotePago, TipoReporte tipoReporte, FormatoReporte formato,
                                  com.fasterxml.jackson.databind.JsonNode contenidoJson, String nombreArchivo,
                                  String hashReporte, OffsetDateTime fechaGeneracion) {
        ReporteCierre reporte = new ReporteCierre();
        reporte.setLotePago(lotePago);
        reporte.setTipoReporte(tipoReporte);
        reporte.setContenidoJson(contenidoJson);
        reporte.setFormatoArchivo(formato);
        reporte.setNombreArchivo(nombreArchivo);
        reporte.setHashReporte(hashReporte);
        reporte.setFechaGeneracion(fechaGeneracion);
        reporte.setDescargadoEmpresa(Boolean.FALSE);
        return reporte;
    }

    private LineaNovedadResponse toLineaNovedadResponse(LineaPagoInternalDto linea) {
        return new LineaNovedadResponse(
                linea.secuencial(),
                linea.estado(),
                linea.codigoError(),
                linea.mensajeError(),
                linea.monto(),
                linea.cuentaDestino(),
                linea.nombreBeneficiario()
        );
    }
}
