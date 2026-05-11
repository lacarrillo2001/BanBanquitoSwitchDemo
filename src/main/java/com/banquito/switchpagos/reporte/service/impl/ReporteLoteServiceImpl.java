package com.banquito.switchpagos.reporte.service.impl;

import com.banquito.switchpagos.auditoria.dto.internal.RegistroAuditoriaRequest;
import com.banquito.switchpagos.auditoria.enums.TipoActorAuditoria;
import com.banquito.switchpagos.auditoria.service.AuditoriaSwitchService;
import com.banquito.switchpagos.common.exception.EstadoInvalidoException;
import com.banquito.switchpagos.common.exception.SolicitudInvalidaException;
import com.banquito.switchpagos.lote.dto.internal.LoteProcesamientoInternalDto;
import com.banquito.switchpagos.lote.enums.EstadoLote;
import com.banquito.switchpagos.lote.model.LotePago;
import com.banquito.switchpagos.lote.service.LotePagoService;
import com.banquito.switchpagos.procesamiento.dto.internal.LineaPagoInternalDto;
import com.banquito.switchpagos.procesamiento.enums.EstadoLineaPago;
import com.banquito.switchpagos.procesamiento.service.LineaPagoService;
import com.banquito.switchpagos.reporte.dto.api.ComprobanteLiquidacionResponse;
import com.banquito.switchpagos.reporte.dto.api.EmpresaComprobanteResponse;
import com.banquito.switchpagos.reporte.dto.api.LineaNovedadResponse;
import com.banquito.switchpagos.reporte.dto.api.LiquidacionComprobanteResponse;
import com.banquito.switchpagos.reporte.dto.api.ReporteNovedadesResponse;
import com.banquito.switchpagos.reporte.dto.api.ResumenNovedadesResponse;
import com.banquito.switchpagos.reporte.dto.api.ResumenPagosComprobanteResponse;
import com.banquito.switchpagos.reporte.enums.FormatoReporte;
import com.banquito.switchpagos.reporte.enums.TipoReporte;
import com.banquito.switchpagos.reporte.model.ReporteCierre;
import com.banquito.switchpagos.reporte.repository.ReporteCierreRepository;
import com.banquito.switchpagos.reporte.service.NotificacionService;
import com.banquito.switchpagos.reporte.service.ReporteLoteService;
import com.banquito.switchpagos.tarifaje.dto.internal.LiquidacionComprobanteInternalDto;
import com.banquito.switchpagos.tarifaje.service.LiquidacionContableService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

@Service
public class ReporteLoteServiceImpl implements ReporteLoteService {

    private static final ZoneId ZONA_HORARIA_OPERATIVA = ZoneId.of("America/Guayaquil");

    private final ReporteCierreRepository reporteCierreRepository;
    private final LotePagoService lotePagoService;
    private final LineaPagoService lineaPagoService;
    private final LiquidacionContableService liquidacionContableService;
    private final NotificacionService notificacionService;
    private final AuditoriaSwitchService auditoriaSwitchService;
    private final ObjectMapper objectMapper;

    public ReporteLoteServiceImpl(ReporteCierreRepository reporteCierreRepository,
                                  LotePagoService lotePagoService,
                                  LineaPagoService lineaPagoService,
                                  LiquidacionContableService liquidacionContableService,
                                  NotificacionService notificacionService,
                                  AuditoriaSwitchService auditoriaSwitchService,
                                  ObjectMapper objectMapper) {
        this.reporteCierreRepository = reporteCierreRepository;
        this.lotePagoService = lotePagoService;
        this.lineaPagoService = lineaPagoService;
        this.liquidacionContableService = liquidacionContableService;
        this.notificacionService = notificacionService;
        this.auditoriaSwitchService = auditoriaSwitchService;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional
    public ReporteNovedadesResponse obtenerOGenerarReporteNovedades(UUID uuidLote, FormatoReporte formato) {
        validarFormatoNovedades(formato);
        return reporteCierreRepository.findByLotePagoUuidLoteAndTipoReporte(uuidLote, TipoReporte.REPORTE_NOVEDADES)
                .map(reporte -> objectMapper.convertValue(reporte.getContenidoJson(), ReporteNovedadesResponse.class))
                .orElseGet(() -> generarReporteNovedades(uuidLote, formato));
    }

    @Override
    @Transactional
    public ComprobanteLiquidacionResponse obtenerOGenerarComprobante(UUID uuidLote, FormatoReporte formato) {
        validarFormatoComprobante(formato);
        return reporteCierreRepository.findByLotePagoUuidLoteAndTipoReporte(uuidLote, TipoReporte.COMPROBANTE_LIQUIDACION)
                .map(reporte -> objectMapper.convertValue(reporte.getContenidoJson(), ComprobanteLiquidacionResponse.class))
                .orElseGet(() -> generarComprobante(uuidLote, formato));
    }

    private ReporteNovedadesResponse generarReporteNovedades(UUID uuidLote, FormatoReporte formato) {
        LoteProcesamientoInternalDto lote = lotePagoService.obtenerDatosProcesamiento(uuidLote);
        validarLoteCerrado(lote);
        notificacionService.registrarNotificacionesBeneficiarios(uuidLote);

        List<LineaPagoInternalDto> lineas = lineaPagoService.listarLineasPorLoteUuid(uuidLote);
        ReporteNovedadesResponse response = new ReporteNovedadesResponse(
                uuidLote,
                TipoReporte.REPORTE_NOVEDADES.name(),
                formato.name(),
                OffsetDateTime.now(ZONA_HORARIA_OPERATIVA),
                new ResumenNovedadesResponse(
                        Long.valueOf(lineas.size()),
                        lineaPagoService.contarLineasPorLoteUuidYEstado(uuidLote, EstadoLineaPago.EXITOSA),
                        lineaPagoService.contarLineasPorLoteUuidYEstado(uuidLote, EstadoLineaPago.RECHAZADA),
                        lineaPagoService.contarLineasPorLoteUuidYEstado(uuidLote, EstadoLineaPago.FALLIDA)
                ),
                lineas.stream().map(this::construirLineaNovedadResponse).toList()
        );
        guardarReporte(uuidLote, lote.idLote(), TipoReporte.REPORTE_NOVEDADES, formato, response);
        registrarAuditoria("GENERACION_REPORTE_NOVEDADES", lote.rucEmpresa(), uuidLote, TipoReporte.REPORTE_NOVEDADES.name());
        return response;
    }

    private ComprobanteLiquidacionResponse generarComprobante(UUID uuidLote, FormatoReporte formato) {
        LoteProcesamientoInternalDto lote = lotePagoService.obtenerDatosProcesamiento(uuidLote);
        validarLoteCerrado(lote);
        LiquidacionComprobanteInternalDto liquidacion = liquidacionContableService.obtenerLiquidacionCompletada(uuidLote);
        BigDecimal montoDispersado = lineaPagoService.sumarMontoPorLoteUuidYEstado(uuidLote, EstadoLineaPago.EXITOSA);
        ComprobanteLiquidacionResponse response = new ComprobanteLiquidacionResponse(
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
                OffsetDateTime.now(ZONA_HORARIA_OPERATIVA)
        );
        guardarReporte(uuidLote, lote.idLote(), TipoReporte.COMPROBANTE_LIQUIDACION, formato, response);
        registrarAuditoria("GENERACION_COMPROBANTE_LIQUIDACION", lote.rucEmpresa(), uuidLote,
                TipoReporte.COMPROBANTE_LIQUIDACION.name());
        return response;
    }

    private void guardarReporte(UUID uuidLote, Long idLote, TipoReporte tipoReporte, FormatoReporte formato,
                                Object response) {
        ReporteCierre reporte = new ReporteCierre();
        reporte.setLotePago(new LotePago(idLote));
        reporte.setTipoReporte(tipoReporte);
        reporte.setContenidoJson(objectMapper.valueToTree(response));
        reporte.setFormatoArchivo(formato);
        reporte.setNombreArchivo(tipoReporte.name().toLowerCase() + "-" + uuidLote + "." + formato.name().toLowerCase());
        reporte.setHashReporte(calcularHashReporte(response));
        reporte.setFechaGeneracion(OffsetDateTime.now(ZONA_HORARIA_OPERATIVA));
        reporte.setDescargadoEmpresa(Boolean.FALSE);
        reporteCierreRepository.save(reporte);
    }

    private LineaNovedadResponse construirLineaNovedadResponse(LineaPagoInternalDto linea) {
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

    private void validarLoteCerrado(LoteProcesamientoInternalDto lote) {
        if (!EstadoLote.CERRADO.equals(lote.estado())) {
            throw new EstadoInvalidoException(
                    "LOTE_NO_CERRADO",
                    "Los reportes de cierre estan disponibles solo para lotes cerrados."
            );
        }
    }

    private void validarFormatoNovedades(FormatoReporte formato) {
        if (!FormatoReporte.JSON.equals(formato) && !FormatoReporte.CSV.equals(formato)
                && !FormatoReporte.XLSX.equals(formato)) {
            throw new SolicitudInvalidaException(
                    "FORMATO_REPORTE_INVALIDO",
                    "El reporte de novedades acepta formatos JSON, CSV o XLSX."
            );
        }
    }

    private void validarFormatoComprobante(FormatoReporte formato) {
        if (!FormatoReporte.JSON.equals(formato) && !FormatoReporte.PDF.equals(formato)) {
            throw new SolicitudInvalidaException(
                    "FORMATO_REPORTE_INVALIDO",
                    "El comprobante acepta formatos JSON o PDF."
            );
        }
    }

    private String calcularHashReporte(Object response) {
        try {
            byte[] contenido = objectMapper.writeValueAsString(response).getBytes(StandardCharsets.UTF_8);
            return java.util.HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(contenido));
        } catch (NoSuchAlgorithmException | com.fasterxml.jackson.core.JsonProcessingException exception) {
            throw new SolicitudInvalidaException(
                    "HASH_REPORTE_NO_CALCULADO",
                    "No fue posible calcular el hash del reporte.",
                    exception
            );
        }
    }

    private void registrarAuditoria(String accion, String rucEmpresa, UUID uuidLote, String tipoReporte) {
        RegistroAuditoriaRequest request = new RegistroAuditoriaRequest();
        request.setTipoActor(TipoActorAuditoria.SISTEMA);
        request.setIdActor("SWITCH");
        request.setRucEmpresa(rucEmpresa);
        request.setAccion(accion);
        request.setEntidad("REPORTE_CIERRE");
        request.setIdEntidad(uuidLote.toString());
        ObjectNode datos = objectMapper.createObjectNode();
        datos.put("tipoReporte", tipoReporte);
        request.setDatosDespues(datos);
        auditoriaSwitchService.registrarAccion(request);
    }
}
