package com.banquito.switchpagos.procesamiento.service.impl;

import com.banquito.switchpagos.common.exception.EstadoInvalidoException;
import com.banquito.switchpagos.common.exception.SwitchPagosException;
import com.banquito.switchpagos.integracioncore.dto.internal.ConsultaSaldoCoreResponse;
import com.banquito.switchpagos.integracioncore.dto.internal.MovimientoCoreRequest;
import com.banquito.switchpagos.integracioncore.dto.internal.MovimientoCoreResponse;
import com.banquito.switchpagos.integracioncore.dto.internal.ValidacionCuentaCoreResponse;
import com.banquito.switchpagos.integracioncore.service.CoreBancarioService;
import com.banquito.switchpagos.lote.dto.internal.LoteProcesamientoInternalDto;
import com.banquito.switchpagos.lote.enums.EstadoLote;
import com.banquito.switchpagos.lote.service.LotePagoService;
import com.banquito.switchpagos.procesamiento.dto.api.ProcesarLoteRequest;
import com.banquito.switchpagos.procesamiento.dto.api.ProcesarLoteResponse;
import com.banquito.switchpagos.procesamiento.dto.api.ResultadoProcesamientoResponse;
import com.banquito.switchpagos.procesamiento.enums.EstadoLineaPago;
import com.banquito.switchpagos.procesamiento.model.LineaPago;
import com.banquito.switchpagos.procesamiento.repository.LineaPagoRepository;
import com.banquito.switchpagos.procesamiento.service.ProcesamientoPagoService;
import com.banquito.switchpagos.procesamiento.service.ValidadorLineaPagoService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

@Service
public class ProcesamientoPagoServiceImpl implements ProcesamientoPagoService {

    private static final ZoneId ZONA_HORARIA_OPERATIVA = ZoneId.of("America/Guayaquil");

    private final LotePagoService lotePagoService;
    private final LineaPagoRepository lineaPagoRepository;
    private final ValidadorLineaPagoService validadorLineaPagoService;
    private final CoreBancarioService coreBancarioService;

    public ProcesamientoPagoServiceImpl(LotePagoService lotePagoService,
                                        LineaPagoRepository lineaPagoRepository,
                                        ValidadorLineaPagoService validadorLineaPagoService,
                                        CoreBancarioService coreBancarioService) {
        this.lotePagoService = lotePagoService;
        this.lineaPagoRepository = lineaPagoRepository;
        this.validadorLineaPagoService = validadorLineaPagoService;
        this.coreBancarioService = coreBancarioService;
    }

    @Override
    @Transactional
    public ProcesarLoteResponse procesarLote(UUID uuidLote, ProcesarLoteRequest procesarLoteRequest) {
        LoteProcesamientoInternalDto loteProcesamiento = lotePagoService.obtenerDatosProcesamiento(uuidLote);
        if (!EstadoLote.VALIDADO.equals(loteProcesamiento.estado())) {
            throw new EstadoInvalidoException(
                    "LOTE_ESTADO_NO_PROCESABLE",
                    "Solo se pueden procesar lotes en estado VALIDADO."
            );
        }

        String ejecutadoPor = obtenerEjecutadoPor(procesarLoteRequest);
        lotePagoService.iniciarProcesamiento(uuidLote, ejecutadoPor);

        List<LineaPago> lineas = lineaPagoRepository.findByLotePagoUuidLoteAndEstadoInOrderBySecuencialAsc(
                uuidLote,
                List.of(EstadoLineaPago.PENDIENTE, EstadoLineaPago.VALIDADA)
        );
        for (LineaPago lineaPago : lineas) {
            procesarLinea(loteProcesamiento, lineaPago);
        }

        ResultadoProcesamientoResponse resultado = construirResultado(uuidLote);
        EstadoLote estadoFinal = determinarEstadoFinal(resultado);
        lotePagoService.finalizarProcesamiento(
                uuidLote,
                estadoFinal,
                resultado.exitosas().intValue(),
                Math.toIntExact(resultado.rechazadas() + resultado.fallidas()),
                resultado.montoProcesadoExitoso(),
                ejecutadoPor
        );

        return new ProcesarLoteResponse(uuidLote, estadoFinal.name(), resultado, "LIQUIDAR");
    }

    private void procesarLinea(LoteProcesamientoInternalDto loteProcesamiento, LineaPago lineaPago) {
        try {
            validarLinea(loteProcesamiento, lineaPago);
            enviarLineaAlCore(loteProcesamiento, lineaPago);
            registrarLineaExitosa(lineaPago);
        } catch (SwitchPagosException exception) {
            registrarLineaRechazada(lineaPago, exception.getCodigo(), exception.getMessage());
        } catch (Exception exception) {
            registrarLineaFallida(lineaPago, "ERROR_TECNICO_LINEA", "Ocurrio un error tecnico al procesar la linea.");
        }
    }

    private void validarLinea(LoteProcesamientoInternalDto loteProcesamiento, LineaPago lineaPago) {
        validadorLineaPagoService.validarLimite(loteProcesamiento.tipoServicio(), lineaPago);
        ConsultaSaldoCoreResponse saldoResponse = coreBancarioService.consultarSaldoDisponible(
                loteProcesamiento.cuentaMatrizCargo()
        );
        if (!Boolean.TRUE.equals(saldoResponse.exitoso())) {
            throw new EstadoInvalidoException(saldoResponse.codigo(), saldoResponse.mensaje());
        }
        if (lineaPago.getMonto().compareTo(saldoResponse.saldoDisponible()) > 0) {
            throw new EstadoInvalidoException(
                    "SALDO_INSUFICIENTE",
                    "La cuenta matriz no tiene saldo suficiente para procesar la linea."
            );
        }
        ValidacionCuentaCoreResponse cuentaResponse = coreBancarioService.validarCuentaDestino(
                lineaPago.getCuentaDestino(),
                lineaPago.getIdentificacionBeneficiario()
        );
        if (!Boolean.TRUE.equals(cuentaResponse.valida())) {
            throw new EstadoInvalidoException(cuentaResponse.codigo(), cuentaResponse.mensaje());
        }
        lineaPago.setEstado(EstadoLineaPago.VALIDADA);
        lineaPago.setFechaValidacion(OffsetDateTime.now(ZONA_HORARIA_OPERATIVA));
        lineaPago.setFechaActualizacion(OffsetDateTime.now(ZONA_HORARIA_OPERATIVA));
        lineaPagoRepository.save(lineaPago);
    }

    private void enviarLineaAlCore(LoteProcesamientoInternalDto loteProcesamiento, LineaPago lineaPago) {
        UUID uuidGrupoCore = UUID.randomUUID();
        lineaPago.setEstado(EstadoLineaPago.ENVIADA_CORE);
        lineaPago.setFechaEnvioCore(OffsetDateTime.now(ZONA_HORARIA_OPERATIVA));
        lineaPago.setUuidGrupoCore(uuidGrupoCore);
        lineaPagoRepository.save(lineaPago);

        MovimientoCoreResponse debitoResponse = coreBancarioService.ejecutarDebito(new MovimientoCoreRequest(
                loteProcesamiento.cuentaMatrizCargo(),
                lineaPago.getCuentaDestino(),
                lineaPago.getMonto(),
                lineaPago.getUuidOperacionSwitch(),
                uuidGrupoCore,
                lineaPago.getConceptoReferencia(),
                Boolean.FALSE
        ));
        if (!Boolean.TRUE.equals(debitoResponse.exitoso())) {
            throw new EstadoInvalidoException(debitoResponse.codigo(), debitoResponse.mensaje());
        }
        lineaPago.setUuidDebitoCore(debitoResponse.uuidTransaccionCore());
        lineaPago.setUuidGrupoCore(debitoResponse.uuidGrupoCore());

        MovimientoCoreResponse creditoResponse = coreBancarioService.ejecutarCredito(new MovimientoCoreRequest(
                loteProcesamiento.cuentaMatrizCargo(),
                lineaPago.getCuentaDestino(),
                lineaPago.getMonto(),
                lineaPago.getUuidOperacionSwitch(),
                debitoResponse.uuidGrupoCore(),
                lineaPago.getConceptoReferencia(),
                Boolean.FALSE
        ));
        if (!Boolean.TRUE.equals(creditoResponse.exitoso())) {
            throw new EstadoInvalidoException(creditoResponse.codigo(), creditoResponse.mensaje());
        }
        lineaPago.setUuidCreditoCore(creditoResponse.uuidTransaccionCore());
        lineaPago.setFechaRespuestaCore(OffsetDateTime.now(ZONA_HORARIA_OPERATIVA));
        lineaPagoRepository.save(lineaPago);
    }

    private void registrarLineaExitosa(LineaPago lineaPago) {
        lineaPago.setEstado(EstadoLineaPago.EXITOSA);
        lineaPago.setCodigoError(null);
        lineaPago.setMensajeError(null);
        lineaPago.setFechaProceso(OffsetDateTime.now(ZONA_HORARIA_OPERATIVA));
        lineaPago.setFechaActualizacion(OffsetDateTime.now(ZONA_HORARIA_OPERATIVA));
        lineaPagoRepository.save(lineaPago);
    }

    private void registrarLineaRechazada(LineaPago lineaPago, String codigoError, String mensajeError) {
        lineaPago.setEstado(EstadoLineaPago.RECHAZADA);
        lineaPago.setCodigoError(codigoError);
        lineaPago.setMensajeError(mensajeError);
        lineaPago.setFechaRespuestaCore(OffsetDateTime.now(ZONA_HORARIA_OPERATIVA));
        lineaPago.setFechaProceso(OffsetDateTime.now(ZONA_HORARIA_OPERATIVA));
        lineaPago.setFechaActualizacion(OffsetDateTime.now(ZONA_HORARIA_OPERATIVA));
        lineaPagoRepository.save(lineaPago);
    }

    private void registrarLineaFallida(LineaPago lineaPago, String codigoError, String mensajeError) {
        lineaPago.setEstado(EstadoLineaPago.FALLIDA);
        lineaPago.setCodigoError(codigoError);
        lineaPago.setMensajeError(mensajeError);
        lineaPago.setFechaRespuestaCore(OffsetDateTime.now(ZONA_HORARIA_OPERATIVA));
        lineaPago.setFechaProceso(OffsetDateTime.now(ZONA_HORARIA_OPERATIVA));
        lineaPago.setFechaActualizacion(OffsetDateTime.now(ZONA_HORARIA_OPERATIVA));
        lineaPagoRepository.save(lineaPago);
    }

    private ResultadoProcesamientoResponse construirResultado(UUID uuidLote) {
        Long totalLineas = lineaPagoRepository.countByLotePagoUuidLote(uuidLote);
        Long exitosas = lineaPagoRepository.countByLotePagoUuidLoteAndEstado(uuidLote, EstadoLineaPago.EXITOSA);
        Long rechazadas = lineaPagoRepository.countByLotePagoUuidLoteAndEstado(uuidLote, EstadoLineaPago.RECHAZADA);
        Long fallidas = lineaPagoRepository.countByLotePagoUuidLoteAndEstado(uuidLote, EstadoLineaPago.FALLIDA);
        BigDecimal montoExitoso = lineaPagoRepository.sumarMontoPorLoteUuidYEstado(uuidLote, EstadoLineaPago.EXITOSA);
        return new ResultadoProcesamientoResponse(totalLineas, exitosas, rechazadas, fallidas, montoExitoso);
    }

    private EstadoLote determinarEstadoFinal(ResultadoProcesamientoResponse resultado) {
        if (resultado.totalLineas().equals(resultado.exitosas())) {
            return EstadoLote.PROCESADO_TOTAL;
        }
        return EstadoLote.PROCESADO_PARCIAL;
    }

    private String obtenerEjecutadoPor(ProcesarLoteRequest procesarLoteRequest) {
        if (procesarLoteRequest == null || procesarLoteRequest.ejecutadoPor() == null
                || procesarLoteRequest.ejecutadoPor().isBlank()) {
            return "SISTEMA";
        }
        return procesarLoteRequest.ejecutadoPor();
    }
}
