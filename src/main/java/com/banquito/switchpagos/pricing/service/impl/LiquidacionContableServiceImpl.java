package com.banquito.switchpagos.pricing.service.impl;

import com.banquito.switchpagos.audit.dto.internal.RegistroAuditoriaRequest;
import com.banquito.switchpagos.audit.enums.TipoActorAuditoria;
import com.banquito.switchpagos.audit.service.AuditoriaSwitchService;
import com.banquito.switchpagos.integrationcore.config.CoreBancarioProperties;
import com.banquito.switchpagos.integrationcore.dto.internal.LiquidacionCoreRequest;
import com.banquito.switchpagos.integrationcore.dto.internal.LiquidacionCoreResponse;
import com.banquito.switchpagos.shared.exception.ConflictoOperacionException;
import com.banquito.switchpagos.shared.exception.EstadoInvalidoException;
import com.banquito.switchpagos.shared.exception.IntegracionCoreException;
import com.banquito.switchpagos.integrationcore.service.CoreBancarioService;
import com.banquito.switchpagos.batch.dto.internal.LoteProcesamientoInternalDto;
import com.banquito.switchpagos.batch.enums.EstadoLote;
import com.banquito.switchpagos.batch.model.LotePago;
import com.banquito.switchpagos.batch.service.LotePagoService;
import com.banquito.switchpagos.pricing.dto.api.LiquidarLoteResponse;
import com.banquito.switchpagos.pricing.dto.internal.CalculoLiquidacionInternalDto;
import com.banquito.switchpagos.pricing.dto.internal.LiquidacionComprobanteInternalDto;
import com.banquito.switchpagos.pricing.dto.internal.MovimientoContableInternalDto;
import com.banquito.switchpagos.pricing.enums.ConceptoDetalleLiquidacion;
import com.banquito.switchpagos.pricing.enums.EstadoDebitoLiquidacion;
import com.banquito.switchpagos.pricing.model.DetalleLiquidacion;
import com.banquito.switchpagos.pricing.model.LiquidacionServicio;
import com.banquito.switchpagos.pricing.mapper.DetalleLiquidacionMapper;
import com.banquito.switchpagos.pricing.mapper.LiquidacionServicioMapper;
import com.banquito.switchpagos.pricing.repository.DetalleLiquidacionRepository;
import com.banquito.switchpagos.pricing.repository.LiquidacionServicioRepository;
import com.banquito.switchpagos.pricing.service.LiquidacionContableService;
import com.banquito.switchpagos.pricing.service.TarifajeService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.persistence.EntityManager;
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
    private final CoreBancarioProperties coreBancarioProperties;
    private final AuditoriaSwitchService auditoriaSwitchService;
    private final ObjectMapper objectMapper;
    private final EntityManager entityManager;
    private final LiquidacionServicioMapper liquidacionServicioMapper;
    private final DetalleLiquidacionMapper detalleLiquidacionMapper;

    public LiquidacionContableServiceImpl(LiquidacionServicioRepository liquidacionServicioRepository,
                                          DetalleLiquidacionRepository detalleLiquidacionRepository,
                                          TarifajeService tarifajeService,
                                          LotePagoService lotePagoService,
                                          CoreBancarioService coreBancarioService,
                                          CoreBancarioProperties coreBancarioProperties,
                                          AuditoriaSwitchService auditoriaSwitchService,
                                          ObjectMapper objectMapper,
                                          EntityManager entityManager,
                                          LiquidacionServicioMapper liquidacionServicioMapper,
                                          DetalleLiquidacionMapper detalleLiquidacionMapper) {
        this.liquidacionServicioRepository = liquidacionServicioRepository;
        this.detalleLiquidacionRepository = detalleLiquidacionRepository;
        this.tarifajeService = tarifajeService;
        this.lotePagoService = lotePagoService;
        this.coreBancarioService = coreBancarioService;
        this.coreBancarioProperties = coreBancarioProperties;
        this.auditoriaSwitchService = auditoriaSwitchService;
        this.objectMapper = objectMapper;
        this.entityManager = entityManager;
        this.liquidacionServicioMapper = liquidacionServicioMapper;
        this.detalleLiquidacionMapper = detalleLiquidacionMapper;
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
        DetalleLiquidacion detalleLiquidacion = detalleLiquidacionMapper.toEntity(
                liquidacionServicio,
                movimientoContableInternalDto,
                OffsetDateTime.now(ZONA_HORARIA_OPERATIVA)
        );
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
        return liquidacionServicioMapper.toComprobanteInternalDto(liquidacionServicio);
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
        LiquidacionServicio liquidacionServicio = liquidacionServicioMapper.toPendienteEntity(
                entityManager.getReference(LotePago.class, loteProcesamiento.idLote()),
                calculo,
                OffsetDateTime.now(ZONA_HORARIA_OPERATIVA)
        );
        return liquidacionServicioRepository.save(liquidacionServicio);
    }

    private List<MovimientoContableInternalDto> ejecutarMovimientosContables(LoteProcesamientoInternalDto loteProcesamiento,
                                                                             CalculoLiquidacionInternalDto calculo) {
        List<MovimientoContableInternalDto> movimientos = new ArrayList<>();
        UUID uuidGrupoCore = UUID.randomUUID();
        LiquidacionCoreRequest liquidacionCoreRequest = new LiquidacionCoreRequest(
                uuidGrupoCore,
                loteProcesamiento.cuentaMatrizCargo(),
                calculo.subtotalComision(),
                calculo.montoIva(),
                calculo.totalDebitado(),
                Boolean.TRUE,
                coreBancarioProperties.getIntegration().getCodigoCuentaIngresos(),
                coreBancarioProperties.getIntegration().getCodigoCuentaIva(),
                loteProcesamiento.uuidLote().toString()
        );
        LiquidacionCoreResponse liquidacionCoreResponse = coreBancarioService.liquidarServicio(liquidacionCoreRequest);
        if (!Boolean.TRUE.equals(liquidacionCoreResponse.exitoso())) {
            throw new IntegracionCoreException(liquidacionCoreResponse.codigo(), liquidacionCoreResponse.mensaje());
        }

        movimientos.add(new MovimientoContableInternalDto(
                ConceptoDetalleLiquidacion.DEBITO_CUENTA_MATRIZ,
                calculo.totalDebitado(),
                liquidacionCoreResponse.uuidDebitoMatriz(),
                loteProcesamiento.cuentaMatrizCargo(),
                null,
                EstadoDebitoLiquidacion.COMPLETADO.name()
        ));
        movimientos.add(new MovimientoContableInternalDto(
                ConceptoDetalleLiquidacion.CREDITO_INGRESOS,
                calculo.subtotalComision(),
                liquidacionCoreResponse.uuidCreditoIngresos(),
                loteProcesamiento.cuentaMatrizCargo(),
                coreBancarioProperties.getIntegration().getNumeroCuentaIngresos(),
                EstadoDebitoLiquidacion.COMPLETADO.name()
        ));
        movimientos.add(new MovimientoContableInternalDto(
                ConceptoDetalleLiquidacion.CREDITO_IVA,
                calculo.montoIva(),
                liquidacionCoreResponse.uuidCreditoIva(),
                loteProcesamiento.cuentaMatrizCargo(),
                coreBancarioProperties.getIntegration().getNumeroCuentaIva(),
                EstadoDebitoLiquidacion.COMPLETADO.name()
        ));
        return movimientos;
    }

    private LiquidarLoteResponse construirLiquidarLoteResponse(UUID uuidLote, LiquidacionServicio liquidacionServicio,
                                                               List<MovimientoContableInternalDto> movimientos) {
        return liquidacionServicioMapper.toLiquidarLoteResponse(uuidLote, liquidacionServicio, movimientos);
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
