package com.banquito.switchpagos.tarifaje.service.impl;

import com.banquito.switchpagos.auditoria.dto.internal.RegistroAuditoriaRequest;
import com.banquito.switchpagos.auditoria.enums.TipoActorAuditoria;
import com.banquito.switchpagos.auditoria.service.AuditoriaSwitchService;
import com.banquito.switchpagos.common.exception.ConflictoOperacionException;
import com.banquito.switchpagos.common.exception.EstadoInvalidoException;
import com.banquito.switchpagos.common.exception.IntegracionCoreException;
import com.banquito.switchpagos.integracioncore.dto.internal.MovimientoCoreRequest;
import com.banquito.switchpagos.integracioncore.dto.internal.MovimientoCoreResponse;
import com.banquito.switchpagos.integracioncore.service.CoreBancarioService;
import com.banquito.switchpagos.lote.dto.internal.LoteProcesamientoInternalDto;
import com.banquito.switchpagos.lote.enums.EstadoLote;
import com.banquito.switchpagos.lote.model.LotePago;
import com.banquito.switchpagos.lote.service.LotePagoService;
import com.banquito.switchpagos.tarifaje.constants.CuentaContableCore;
import com.banquito.switchpagos.tarifaje.dto.api.LiquidarLoteResponse;
import com.banquito.switchpagos.tarifaje.dto.api.MovimientoContableResponse;
import com.banquito.switchpagos.tarifaje.dto.internal.CalculoLiquidacionInternalDto;
import com.banquito.switchpagos.tarifaje.dto.internal.LiquidacionComprobanteInternalDto;
import com.banquito.switchpagos.tarifaje.dto.internal.MovimientoContableInternalDto;
import com.banquito.switchpagos.tarifaje.enums.ConceptoDetalleLiquidacion;
import com.banquito.switchpagos.tarifaje.enums.EstadoDebitoLiquidacion;
import com.banquito.switchpagos.tarifaje.model.DetalleLiquidacion;
import com.banquito.switchpagos.tarifaje.model.LiquidacionServicio;
import com.banquito.switchpagos.tarifaje.repository.DetalleLiquidacionRepository;
import com.banquito.switchpagos.tarifaje.repository.LiquidacionServicioRepository;
import com.banquito.switchpagos.tarifaje.service.LiquidacionContableService;
import com.banquito.switchpagos.tarifaje.service.TarifajeService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class LiquidacionContableServiceImpl implements LiquidacionContableService {

    private static final ZoneId ZONA_HORARIA_OPERATIVA = ZoneId.of("America/Guayaquil");

    private final LiquidacionServicioRepository liquidacionServicioRepository;
    private final DetalleLiquidacionRepository detalleLiquidacionRepository;
    private final TarifajeService tarifajeService;
    private final LotePagoService lotePagoService;
    private final CoreBancarioService coreBancarioService;
    private final AuditoriaSwitchService auditoriaSwitchService;
    private final ObjectMapper objectMapper;

    public LiquidacionContableServiceImpl(LiquidacionServicioRepository liquidacionServicioRepository,
                                          DetalleLiquidacionRepository detalleLiquidacionRepository,
                                          TarifajeService tarifajeService,
                                          LotePagoService lotePagoService,
                                          CoreBancarioService coreBancarioService,
                                          AuditoriaSwitchService auditoriaSwitchService,
                                          ObjectMapper objectMapper) {
        this.liquidacionServicioRepository = liquidacionServicioRepository;
        this.detalleLiquidacionRepository = detalleLiquidacionRepository;
        this.tarifajeService = tarifajeService;
        this.lotePagoService = lotePagoService;
        this.coreBancarioService = coreBancarioService;
        this.auditoriaSwitchService = auditoriaSwitchService;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional(noRollbackFor = IntegracionCoreException.class)
    public LiquidarLoteResponse liquidarServicio(UUID uuidLote) {
        LoteProcesamientoInternalDto loteProcesamiento = lotePagoService.obtenerDatosProcesamiento(uuidLote);
        validarLoteLiquidable(loteProcesamiento);
        if (Boolean.TRUE.equals(liquidacionServicioRepository.existsByLotePagoUuidLote(uuidLote))) {
            throw new ConflictoOperacionException(
                    "LIQUIDACION_YA_EXISTE",
                    "El lote ya tiene una liquidacion registrada."
            );
        }

        CalculoLiquidacionInternalDto calculo = tarifajeService.calcularLiquidacion(uuidLote);
        registrarAuditoria(uuidLote, loteProcesamiento.rucEmpresa(), "CALCULO_COMISION", construirDatosCalculo(calculo));
        LiquidacionServicio liquidacionServicio = crearLiquidacionPendiente(loteProcesamiento, calculo);
        try {
            List<MovimientoContableInternalDto> movimientos = ejecutarMovimientosContables(
                    loteProcesamiento,
                    calculo
            );
            liquidacionServicio.setEstadoDebito(EstadoDebitoLiquidacion.COMPLETADO);
            liquidacionServicio.setFechaLiquidacion(OffsetDateTime.now(ZONA_HORARIA_OPERATIVA));
            liquidacionServicio.setFechaActualizacion(OffsetDateTime.now(ZONA_HORARIA_OPERATIVA));
            liquidacionServicioRepository.save(liquidacionServicio);
            movimientos.forEach(movimiento -> registrarDetalleLiquidacion(liquidacionServicio, movimiento));
            lotePagoService.cerrarLoteLiquidado(uuidLote, "SISTEMA");
            registrarAuditoria(uuidLote, loteProcesamiento.rucEmpresa(), "LIQUIDACION_COMPLETADA",
                    construirDatosLiquidacion(liquidacionServicio));
            return construirLiquidarLoteResponse(uuidLote, liquidacionServicio, movimientos);
        } catch (IntegracionCoreException exception) {
            liquidacionServicio.setEstadoDebito(EstadoDebitoLiquidacion.RECHAZADO);
            liquidacionServicio.setFechaActualizacion(OffsetDateTime.now(ZONA_HORARIA_OPERATIVA));
            liquidacionServicioRepository.save(liquidacionServicio);
            registrarAuditoria(uuidLote, loteProcesamiento.rucEmpresa(), "LIQUIDACION_RECHAZADA",
                    construirDatosLiquidacion(liquidacionServicio));
            throw exception;
        }
    }

    @Override
    public void registrarDetalleLiquidacion(LiquidacionServicio liquidacionServicio,
                                            MovimientoContableInternalDto movimientoContableInternalDto) {
        DetalleLiquidacion detalleLiquidacion = new DetalleLiquidacion();
        detalleLiquidacion.setLiquidacionServicio(liquidacionServicio);
        detalleLiquidacion.setConcepto(movimientoContableInternalDto.concepto());
        detalleLiquidacion.setMonto(movimientoContableInternalDto.monto());
        detalleLiquidacion.setUuidTransaccionCore(movimientoContableInternalDto.uuidTransaccionCore());
        detalleLiquidacion.setCuentaOrigenCore(movimientoContableInternalDto.cuentaOrigenCore());
        detalleLiquidacion.setCuentaDestinoCore(movimientoContableInternalDto.cuentaDestinoCore());
        detalleLiquidacion.setFechaCreacion(OffsetDateTime.now(ZONA_HORARIA_OPERATIVA));
        detalleLiquidacionRepository.save(detalleLiquidacion);
    }

    @Override
    @Transactional(readOnly = true)
    public LiquidacionComprobanteInternalDto obtenerLiquidacionCompletada(UUID uuidLote) {
        LiquidacionServicio liquidacionServicio = liquidacionServicioRepository.findByLotePagoUuidLote(uuidLote)
                .orElseThrow(() -> new EstadoInvalidoException(
                        "LIQUIDACION_NO_ENCONTRADA",
                        "El lote no tiene una liquidacion registrada."
                ));
        if (!EstadoDebitoLiquidacion.COMPLETADO.equals(liquidacionServicio.getEstadoDebito())) {
            throw new EstadoInvalidoException(
                    "LIQUIDACION_NO_COMPLETADA",
                    "El comprobante requiere una liquidacion completada."
            );
        }
        return new LiquidacionComprobanteInternalDto(
                liquidacionServicio.getTransaccionesExitosas(),
                liquidacionServicio.getTransaccionesFallidas(),
                liquidacionServicio.getTarifaUnitariaAplicada(),
                liquidacionServicio.getIvaPorcentajeAplicado(),
                liquidacionServicio.getSubtotalComision(),
                liquidacionServicio.getMontoIva(),
                liquidacionServicio.getTotalDebitado(),
                liquidacionServicio.getFechaLiquidacion()
        );
    }

    private void validarLoteLiquidable(LoteProcesamientoInternalDto loteProcesamiento) {
        if (!EstadoLote.PROCESADO_PARCIAL.equals(loteProcesamiento.estado())
                && !EstadoLote.PROCESADO_TOTAL.equals(loteProcesamiento.estado())) {
            throw new EstadoInvalidoException(
                    "LOTE_ESTADO_NO_LIQUIDABLE",
                    "Solo se pueden liquidar lotes procesados parcial o totalmente."
            );
        }
    }

    private LiquidacionServicio crearLiquidacionPendiente(LoteProcesamientoInternalDto loteProcesamiento,
                                                          CalculoLiquidacionInternalDto calculo) {
        LiquidacionServicio liquidacionServicio = new LiquidacionServicio();
        liquidacionServicio.setLotePago(new LotePago(loteProcesamiento.idLote()));
        liquidacionServicio.setTarifaAplicada(calculo.tarifaServicio());
        liquidacionServicio.setTransaccionesExitosas(calculo.transaccionesExitosas());
        liquidacionServicio.setTransaccionesFallidas(calculo.transaccionesFallidas());
        liquidacionServicio.setTarifaUnitariaAplicada(calculo.tarifaUnitariaAplicada());
        liquidacionServicio.setIvaPorcentajeAplicado(calculo.ivaPorcentajeAplicado());
        liquidacionServicio.setSubtotalComision(calculo.subtotalComision());
        liquidacionServicio.setMontoIva(calculo.montoIva());
        liquidacionServicio.setTotalDebitado(calculo.totalDebitado());
        liquidacionServicio.setEstadoDebito(EstadoDebitoLiquidacion.PENDIENTE);
        liquidacionServicio.setPermiteSobregiro(Boolean.TRUE);
        liquidacionServicio.setFechaCreacion(OffsetDateTime.now(ZONA_HORARIA_OPERATIVA));
        return liquidacionServicioRepository.save(liquidacionServicio);
    }

    private List<MovimientoContableInternalDto> ejecutarMovimientosContables(LoteProcesamientoInternalDto loteProcesamiento,
                                                                             CalculoLiquidacionInternalDto calculo) {
        List<MovimientoContableInternalDto> movimientos = new ArrayList<>();
        UUID uuidGrupoCore = UUID.randomUUID();
        movimientos.add(ejecutarMovimiento(
                ConceptoDetalleLiquidacion.DEBITO_CUENTA_MATRIZ,
                loteProcesamiento.cuentaMatrizCargo(),
                CuentaContableCore.INGRESOS_SERVICIOS_MASIVOS,
                calculo.totalDebitado(),
                uuidGrupoCore,
                Boolean.TRUE
        ));
        movimientos.add(ejecutarMovimiento(
                ConceptoDetalleLiquidacion.CREDITO_INGRESOS,
                loteProcesamiento.cuentaMatrizCargo(),
                CuentaContableCore.INGRESOS_SERVICIOS_MASIVOS,
                calculo.subtotalComision(),
                uuidGrupoCore,
                Boolean.TRUE
        ));
        movimientos.add(ejecutarMovimiento(
                ConceptoDetalleLiquidacion.CREDITO_IVA,
                loteProcesamiento.cuentaMatrizCargo(),
                CuentaContableCore.PASIVOS_IVA_RETENIDO,
                calculo.montoIva(),
                uuidGrupoCore,
                Boolean.TRUE
        ));
        return movimientos;
    }

    private MovimientoContableInternalDto ejecutarMovimiento(ConceptoDetalleLiquidacion concepto, String cuentaOrigen,
                                                            String cuentaDestino, java.math.BigDecimal monto,
                                                            UUID uuidGrupoCore, Boolean permiteSobregiro) {
        MovimientoCoreRequest movimientoCoreRequest = new MovimientoCoreRequest(
                cuentaOrigen,
                cuentaDestino,
                monto,
                UUID.randomUUID(),
                uuidGrupoCore,
                concepto.name(),
                permiteSobregiro
        );
        MovimientoCoreResponse movimientoCoreResponse = ConceptoDetalleLiquidacion.DEBITO_CUENTA_MATRIZ.equals(concepto)
                ? coreBancarioService.ejecutarDebito(movimientoCoreRequest)
                : coreBancarioService.ejecutarCredito(movimientoCoreRequest);
        if (!Boolean.TRUE.equals(movimientoCoreResponse.exitoso())) {
            throw new IntegracionCoreException(movimientoCoreResponse.codigo(), movimientoCoreResponse.mensaje());
        }
        return new MovimientoContableInternalDto(
                concepto,
                monto,
                movimientoCoreResponse.uuidTransaccionCore(),
                cuentaOrigen,
                cuentaDestino,
                EstadoDebitoLiquidacion.COMPLETADO.name()
        );
    }

    private LiquidarLoteResponse construirLiquidarLoteResponse(UUID uuidLote, LiquidacionServicio liquidacionServicio,
                                                               List<MovimientoContableInternalDto> movimientos) {
        List<MovimientoContableResponse> movimientosResponse = movimientos.stream()
                .map(movimiento -> new MovimientoContableResponse(
                        movimiento.concepto().name(),
                        movimiento.monto(),
                        movimiento.estado()
                ))
                .toList();
        return new LiquidarLoteResponse(
                uuidLote,
                liquidacionServicio.getEstadoDebito().name(),
                liquidacionServicio.getTransaccionesExitosas(),
                liquidacionServicio.getTransaccionesFallidas(),
                liquidacionServicio.getTarifaUnitariaAplicada(),
                liquidacionServicio.getIvaPorcentajeAplicado(),
                liquidacionServicio.getSubtotalComision(),
                liquidacionServicio.getMontoIva(),
                liquidacionServicio.getTotalDebitado(),
                liquidacionServicio.getPermiteSobregiro(),
                movimientosResponse,
                "GENERAR_REPORTES"
        );
    }

    private void registrarAuditoria(UUID uuidLote, String rucEmpresa, String accion, ObjectNode datosDespues) {
        RegistroAuditoriaRequest registroAuditoriaRequest = new RegistroAuditoriaRequest();
        registroAuditoriaRequest.setTipoActor(TipoActorAuditoria.SISTEMA);
        registroAuditoriaRequest.setIdActor("SWITCH");
        registroAuditoriaRequest.setRucEmpresa(rucEmpresa);
        registroAuditoriaRequest.setAccion(accion);
        registroAuditoriaRequest.setEntidad("LIQUIDACION_SERVICIO");
        registroAuditoriaRequest.setIdEntidad(uuidLote.toString());
        registroAuditoriaRequest.setDatosDespues(datosDespues);
        auditoriaSwitchService.registrarAccion(registroAuditoriaRequest);
    }

    private ObjectNode construirDatosCalculo(CalculoLiquidacionInternalDto calculo) {
        ObjectNode datos = objectMapper.createObjectNode();
        datos.put("transaccionesExitosas", calculo.transaccionesExitosas());
        datos.put("transaccionesFallidas", calculo.transaccionesFallidas());
        datos.put("tarifaUnitariaAplicada", calculo.tarifaUnitariaAplicada());
        datos.put("ivaPorcentajeAplicado", calculo.ivaPorcentajeAplicado());
        datos.put("subtotalComision", calculo.subtotalComision());
        datos.put("montoIva", calculo.montoIva());
        datos.put("totalDebitado", calculo.totalDebitado());
        return datos;
    }

    private ObjectNode construirDatosLiquidacion(LiquidacionServicio liquidacionServicio) {
        ObjectNode datos = objectMapper.createObjectNode();
        datos.put("idLiquidacion", liquidacionServicio.getIdLiquidacion());
        datos.put("estadoDebito", liquidacionServicio.getEstadoDebito().name());
        datos.put("totalDebitado", liquidacionServicio.getTotalDebitado());
        return datos;
    }
}
