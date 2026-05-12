package com.banquito.switchpagos.report.service.impl;

import com.banquito.switchpagos.audit.dto.internal.RegistroAuditoriaRequest;
import com.banquito.switchpagos.audit.enums.TipoActorAuditoria;
import com.banquito.switchpagos.audit.service.AuditoriaSwitchService;
import com.banquito.switchpagos.shared.exception.EstadoInvalidoException;
import com.banquito.switchpagos.shared.exception.SolicitudInvalidaException;
import com.banquito.switchpagos.batch.dto.internal.LoteProcesamientoInternalDto;
import com.banquito.switchpagos.batch.enums.EstadoLote;
import com.banquito.switchpagos.batch.model.LotePago;
import com.banquito.switchpagos.batch.service.LotePagoService;
import com.banquito.switchpagos.processing.dto.internal.LineaPagoInternalDto;
import com.banquito.switchpagos.processing.enums.EstadoLineaPago;
import com.banquito.switchpagos.processing.service.LineaPagoService;
import com.banquito.switchpagos.report.dto.api.ComprobanteLiquidacionResponse;
import com.banquito.switchpagos.report.dto.api.ReporteNovedadesResponse;
import com.banquito.switchpagos.report.dto.api.ResumenNovedadesResponse;
import com.banquito.switchpagos.report.enums.FormatoReporte;
import com.banquito.switchpagos.report.enums.TipoReporte;
import com.banquito.switchpagos.report.mapper.ReporteCierreMapper;
import com.banquito.switchpagos.report.model.ReporteCierre;
import com.banquito.switchpagos.report.repository.ReporteCierreRepository;
import com.banquito.switchpagos.report.service.NotificacionService;
import com.banquito.switchpagos.report.service.ReporteLoteService;
import com.banquito.switchpagos.pricing.dto.internal.LiquidacionComprobanteInternalDto;
import com.banquito.switchpagos.pricing.service.LiquidacionContableService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.persistence.EntityManager;
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
    private final EntityManager entityManager;
    private final ReporteCierreMapper reporteCierreMapper;

    public ReporteLoteServiceImpl(ReporteCierreRepository reporteCierreRepository,
                                  LotePagoService lotePagoService,
                                  LineaPagoService lineaPagoService,
                                  LiquidacionContableService liquidacionContableService,
                                  NotificacionService notificacionService,
                                  AuditoriaSwitchService auditoriaSwitchService,
                                  ObjectMapper objectMapper,
                                  EntityManager entityManager,
                                  ReporteCierreMapper reporteCierreMapper) {
        this.reporteCierreRepository = reporteCierreRepository;
        this.lotePagoService = lotePagoService;
        this.lineaPagoService = lineaPagoService;
        this.liquidacionContableService = liquidacionContableService;
        this.notificacionService = notificacionService;
        this.auditoriaSwitchService = auditoriaSwitchService;
        this.objectMapper = objectMapper;
        this.entityManager = entityManager;
        this.reporteCierreMapper = reporteCierreMapper;
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
        ReporteNovedadesResponse response = reporteCierreMapper.toNovedadesResponse(
                uuidLote,
                formato,
                new ResumenNovedadesResponse(
                        Long.valueOf(lineas.size()),
                        lineaPagoService.contarLineasPorLoteUuidYEstado(uuidLote, EstadoLineaPago.EXITOSA),
                        lineaPagoService.contarLineasPorLoteUuidYEstado(uuidLote, EstadoLineaPago.RECHAZADA),
                        lineaPagoService.contarLineasPorLoteUuidYEstado(uuidLote, EstadoLineaPago.FALLIDA)
                ),
                lineas,
                OffsetDateTime.now(ZONA_HORARIA_OPERATIVA)
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
        ComprobanteLiquidacionResponse response = reporteCierreMapper.toComprobanteResponse(
                uuidLote,
                formato,
                lote,
                liquidacion,
                montoDispersado,
                OffsetDateTime.now(ZONA_HORARIA_OPERATIVA)
        );
        guardarReporte(uuidLote, lote.idLote(), TipoReporte.COMPROBANTE_LIQUIDACION, formato, response);
        registrarAuditoria("GENERACION_COMPROBANTE_LIQUIDACION", lote.rucEmpresa(), uuidLote,
                TipoReporte.COMPROBANTE_LIQUIDACION.name());
        return response;
    }

    private void guardarReporte(UUID uuidLote, Long idLote, TipoReporte tipoReporte, FormatoReporte formato,
                                Object response) {
        ReporteCierre reporte = reporteCierreMapper.toEntity(
                entityManager.getReference(LotePago.class, idLote),
                tipoReporte,
                formato,
                objectMapper.valueToTree(response),
                tipoReporte.name().toLowerCase() + "-" + uuidLote + "." + formato.name().toLowerCase(),
                calcularHashReporte(response),
                OffsetDateTime.now(ZONA_HORARIA_OPERATIVA)
        );
        reporteCierreRepository.save(reporte);
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
