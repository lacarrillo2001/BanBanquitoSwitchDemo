package com.banquito.switchpagos.batch.service.impl;

import com.banquito.switchpagos.batch.dto.api.ProcesarPendientesColaResponse;
import com.banquito.switchpagos.batch.dto.api.ResultadoProcesamientoColaResponse;
import com.banquito.switchpagos.batch.dto.api.ValidacionLoteResponse;
import com.banquito.switchpagos.batch.dto.internal.LoteProcesamientoInternalDto;
import com.banquito.switchpagos.batch.enums.EstadoColaProcesamiento;
import com.banquito.switchpagos.batch.enums.EstadoLote;
import com.banquito.switchpagos.batch.model.ColaProcesamiento;
import com.banquito.switchpagos.batch.repository.ColaProcesamientoRepository;
import com.banquito.switchpagos.batch.service.ColaProcesamientoService;
import com.banquito.switchpagos.batch.service.LotePagoService;
import com.banquito.switchpagos.processing.dto.api.ProcesarLoteRequest;
import com.banquito.switchpagos.processing.service.ProcesamientoPagoService;
import com.banquito.switchpagos.shared.exception.SwitchPagosException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@Service
public class ColaProcesamientoServiceImpl implements ColaProcesamientoService {

    private static final ZoneId ZONA_HORARIA_OPERATIVA = ZoneId.of("America/Guayaquil");
    private static final String ACTOR_COLA = "SCHEDULER_COLA";

    private final ColaProcesamientoRepository colaProcesamientoRepository;
    private final LotePagoService lotePagoService;
    private final ProcesamientoPagoService procesamientoPagoService;
    private final Integer maxLotesPorCiclo;
    private final Integer reintentoDelayMinutos;

    public ColaProcesamientoServiceImpl(ColaProcesamientoRepository colaProcesamientoRepository,
                                        LotePagoService lotePagoService,
                                        ProcesamientoPagoService procesamientoPagoService,
                                        @Value("${switch.cola.max-lotes-por-ciclo:10}") Integer maxLotesPorCiclo,
                                        @Value("${switch.cola.reintento-delay-minutos:5}") Integer reintentoDelayMinutos) {
        this.colaProcesamientoRepository = colaProcesamientoRepository;
        this.lotePagoService = lotePagoService;
        this.procesamientoPagoService = procesamientoPagoService;
        this.maxLotesPorCiclo = maxLotesPorCiclo;
        this.reintentoDelayMinutos = reintentoDelayMinutos;
    }

    @Override
    public ProcesarPendientesColaResponse procesarPendientesVencidos() {
        List<ColaProcesamiento> pendientes = colaProcesamientoRepository
                .findByEstadoColaInAndFechaProgramadaProcesoLessThanEqualOrderByPrioridadAscFechaProgramadaProcesoAsc(
                        estadosProcesables(),
                        OffsetDateTime.now(ZONA_HORARIA_OPERATIVA),
                        PageRequest.of(0, maxLotesPorCiclo)
                );
        return procesarColas(pendientes);
    }

    @Override
    public ProcesarPendientesColaResponse procesarPendientesManual() {
        List<ColaProcesamiento> pendientes = colaProcesamientoRepository
                .findByEstadoColaInOrderByPrioridadAscFechaProgramadaProcesoAsc(
                        estadosProcesables(),
                        PageRequest.of(0, maxLotesPorCiclo)
                );
        return procesarColas(pendientes);
    }

    private List<EstadoColaProcesamiento> estadosProcesables() {
        return List.of(EstadoColaProcesamiento.PENDIENTE, EstadoColaProcesamiento.REINTENTO);
    }

    private ProcesarPendientesColaResponse procesarColas(List<ColaProcesamiento> colas) {
        List<ResultadoProcesamientoColaResponse> resultados = new ArrayList<>();
        Integer completados = 0;
        Integer fallidos = 0;

        for (ColaProcesamiento colaProcesamiento : colas) {
            ResultadoProcesamientoColaResponse resultado = procesarCola(colaProcesamiento);
            resultados.add(resultado);
            if (EstadoColaProcesamiento.COMPLETADO.name().equals(resultado.estadoCola())) {
                completados++;
            }
            if (EstadoColaProcesamiento.FALLIDO.name().equals(resultado.estadoCola())
                    || EstadoColaProcesamiento.REINTENTO.name().equals(resultado.estadoCola())) {
                fallidos++;
            }
        }

        return new ProcesarPendientesColaResponse(colas.size(), completados, fallidos, resultados);
    }

    private ResultadoProcesamientoColaResponse procesarCola(ColaProcesamiento colaProcesamiento) {
        tomarCola(colaProcesamiento);
        try {
            LoteProcesamientoInternalDto lote = lotePagoService.obtenerDatosProcesamiento(
                    colaProcesamiento.getLotePago().getUuidLote()
            );
            if (EstadoLote.RECIBIDO.equals(lote.estado()) || EstadoLote.ENCOLADO.equals(lote.estado())) {
                ValidacionLoteResponse validacion = lotePagoService.validarLote(colaProcesamiento.getLotePago().getUuidLote());
                if (!Boolean.TRUE.equals(validacion.valido())) {
                    completarCola(colaProcesamiento, "LOTE_RECHAZADO_EN_VALIDACION");
                    return construirResultado(colaProcesamiento, "LOTE_RECHAZADO_EN_VALIDACION",
                            "El lote fue rechazado durante la validacion automatica.");
                }
            } else if (!EstadoLote.VALIDADO.equals(lote.estado())) {
                completarCola(colaProcesamiento, "LOTE_ESTADO_NO_PROCESABLE");
                return construirResultado(colaProcesamiento, "LOTE_ESTADO_NO_PROCESABLE",
                        "El lote encolado ya no esta en un estado procesable.");
            }

            procesamientoPagoService.procesarLote(
                    colaProcesamiento.getLotePago().getUuidLote(),
                    new ProcesarLoteRequest(ACTOR_COLA, "Procesamiento automatico de lote encolado.")
            );
            completarCola(colaProcesamiento, null);
            return construirResultado(colaProcesamiento, "LOTE_PROCESADO",
                    "Lote encolado procesado correctamente.");
        } catch (SwitchPagosException exception) {
            registrarFallo(colaProcesamiento, exception.getCodigo() + ": " + exception.getMessage());
            return construirResultado(colaProcesamiento, exception.getCodigo(), exception.getMessage());
        } catch (RuntimeException exception) {
            registrarFallo(colaProcesamiento, "ERROR_TECNICO_COLA: " + exception.getMessage());
            return construirResultado(colaProcesamiento, "ERROR_TECNICO_COLA",
                    "Ocurrio un error tecnico procesando la cola.");
        }
    }

    private void tomarCola(ColaProcesamiento colaProcesamiento) {
        colaProcesamiento.setEstadoCola(EstadoColaProcesamiento.PROCESANDO);
        colaProcesamiento.setTomadoPor(ACTOR_COLA);
        colaProcesamiento.setTomadoEn(OffsetDateTime.now(ZONA_HORARIA_OPERATIVA));
        colaProcesamiento.setIntentos(valorEntero(colaProcesamiento.getIntentos()) + 1);
        colaProcesamiento.setFechaActualizacion(OffsetDateTime.now(ZONA_HORARIA_OPERATIVA));
        colaProcesamientoRepository.save(colaProcesamiento);
    }

    private void completarCola(ColaProcesamiento colaProcesamiento, String ultimoError) {
        colaProcesamiento.setEstadoCola(EstadoColaProcesamiento.COMPLETADO);
        colaProcesamiento.setUltimoError(ultimoError);
        colaProcesamiento.setProximoReintentoEn(null);
        colaProcesamiento.setFechaActualizacion(OffsetDateTime.now(ZONA_HORARIA_OPERATIVA));
        colaProcesamientoRepository.save(colaProcesamiento);
    }

    private void registrarFallo(ColaProcesamiento colaProcesamiento, String ultimoError) {
        Integer intentos = valorEntero(colaProcesamiento.getIntentos());
        Integer maxIntentos = valorEntero(colaProcesamiento.getMaxIntentos());
        if (intentos < maxIntentos) {
            colaProcesamiento.setEstadoCola(EstadoColaProcesamiento.REINTENTO);
            colaProcesamiento.setProximoReintentoEn(
                    OffsetDateTime.now(ZONA_HORARIA_OPERATIVA).plusMinutes(reintentoDelayMinutos)
            );
        } else {
            colaProcesamiento.setEstadoCola(EstadoColaProcesamiento.FALLIDO);
            colaProcesamiento.setProximoReintentoEn(null);
        }
        colaProcesamiento.setUltimoError(recortar(ultimoError, 500));
        colaProcesamiento.setFechaActualizacion(OffsetDateTime.now(ZONA_HORARIA_OPERATIVA));
        colaProcesamientoRepository.save(colaProcesamiento);
    }

    private ResultadoProcesamientoColaResponse construirResultado(ColaProcesamiento colaProcesamiento,
                                                                  String codigo,
                                                                  String mensaje) {
        EstadoLote estadoLote = colaProcesamiento.getLotePago().getEstado();
        return new ResultadoProcesamientoColaResponse(
                colaProcesamiento.getIdCola(),
                colaProcesamiento.getLotePago().getUuidLote(),
                colaProcesamiento.getEstadoCola().name(),
                estadoLote != null ? estadoLote.name() : null,
                codigo,
                mensaje
        );
    }

    private Integer valorEntero(Integer valor) {
        return valor != null ? valor : 0;
    }

    private String recortar(String valor, Integer longitudMaxima) {
        if (valor == null || valor.length() <= longitudMaxima) {
            return valor;
        }
        return valor.substring(0, longitudMaxima);
    }
}
