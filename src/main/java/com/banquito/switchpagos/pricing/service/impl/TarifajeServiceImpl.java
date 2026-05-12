package com.banquito.switchpagos.pricing.service.impl;

import com.banquito.switchpagos.shared.exception.ReglaNegocioException;
import com.banquito.switchpagos.batch.dto.internal.LoteProcesamientoInternalDto;
import com.banquito.switchpagos.batch.service.LotePagoService;
import com.banquito.switchpagos.parameter.constants.CodigoParametroSwitch;
import com.banquito.switchpagos.parameter.service.ParametroSwitchService;
import com.banquito.switchpagos.processing.enums.EstadoLineaPago;
import com.banquito.switchpagos.processing.service.LineaPagoService;
import com.banquito.switchpagos.pricing.dto.api.TarifaServicioResponse;
import com.banquito.switchpagos.pricing.dto.internal.CalculoLiquidacionInternalDto;
import com.banquito.switchpagos.pricing.enums.EstadoTarifaServicio;
import com.banquito.switchpagos.pricing.mapper.TarifaServicioMapper;
import com.banquito.switchpagos.pricing.model.TarifaServicio;
import com.banquito.switchpagos.pricing.repository.TarifaServicioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class TarifajeServiceImpl implements com.banquito.switchpagos.pricing.service.TarifajeService {

    private static final Integer ESCALA_MONTO = 2;

    private final TarifaServicioRepository tarifaServicioRepository;
    private final ParametroSwitchService parametroSwitchService;
    private final LineaPagoService lineaPagoService;
    private final LotePagoService lotePagoService;
    private final TarifaServicioMapper tarifaServicioMapper;

    public TarifajeServiceImpl(TarifaServicioRepository tarifaServicioRepository,
                               ParametroSwitchService parametroSwitchService,
                               LineaPagoService lineaPagoService,
                               LotePagoService lotePagoService,
                               TarifaServicioMapper tarifaServicioMapper) {
        this.tarifaServicioRepository = tarifaServicioRepository;
        this.parametroSwitchService = parametroSwitchService;
        this.lineaPagoService = lineaPagoService;
        this.lotePagoService = lotePagoService;
        this.tarifaServicioMapper = tarifaServicioMapper;
    }

    @Override
    public List<TarifaServicioResponse> consultarTarifasVigentes(String tipoServicio) {
        List<TarifaServicio> tarifas = tarifaServicioRepository.consultarTarifasVigentes(
                normalizarTipoServicio(tipoServicio),
                EstadoTarifaServicio.ACTIVA,
                LocalDate.now()
        );
        Map<String, List<TarifaServicio>> tarifasPorServicio = tarifas.stream()
                .collect(LinkedHashMap::new,
                        (mapa, tarifa) -> mapa.computeIfAbsent(tarifa.getTipoServicio().getCodigo(), clave -> new java.util.ArrayList<>()).add(tarifa),
                        Map::putAll);
        return tarifasPorServicio.entrySet().stream()
                .map(entry -> tarifaServicioMapper.toResponse(entry.getKey(), entry.getValue()))
                .toList();
    }

    @Override
    public TarifaServicio calcularTarifaAplicable(String tipoServicio, Integer transaccionesExitosas) {
        if (transaccionesExitosas == null || transaccionesExitosas < 1) {
            throw new ReglaNegocioException(
                    "SIN_TRANSACCIONES_EXITOSAS",
                    "No se puede liquidar un lote sin transacciones exitosas."
            );
        }
        return tarifaServicioRepository.buscarTarifaAplicable(
                        tipoServicio,
                        transaccionesExitosas,
                        EstadoTarifaServicio.ACTIVA,
                        LocalDate.now()
                )
                .orElseThrow(() -> new ReglaNegocioException(
                        "TARIFA_NO_CONFIGURADA",
                        "No existe una tarifa vigente aplicable para el volumen de transacciones exitosas."
                ));
    }

    @Override
    public CalculoLiquidacionInternalDto calcularLiquidacion(UUID uuidLote) {
        LoteProcesamientoInternalDto loteProcesamiento = lotePagoService.obtenerDatosProcesamiento(uuidLote);
        Long exitosas = lineaPagoService.contarLineasPorLoteUuidYEstado(uuidLote, EstadoLineaPago.EXITOSA);
        Long rechazadas = lineaPagoService.contarLineasPorLoteUuidYEstado(uuidLote, EstadoLineaPago.RECHAZADA);
        Long fallidas = lineaPagoService.contarLineasPorLoteUuidYEstado(uuidLote, EstadoLineaPago.FALLIDA);
        Integer transaccionesExitosas = Math.toIntExact(exitosas);
        Integer transaccionesFallidas = Math.toIntExact(rechazadas + fallidas);
        TarifaServicio tarifaServicio = calcularTarifaAplicable(loteProcesamiento.tipoServicio(), transaccionesExitosas);
        BigDecimal ivaPorcentaje = parametroSwitchService.obtenerDecimal(CodigoParametroSwitch.IVA_PORCENTAJE);
        BigDecimal tarifaUnitaria = tarifaServicio.getTarifaUnitaria();
        BigDecimal subtotalComision = tarifaUnitaria
                .multiply(BigDecimal.valueOf(transaccionesExitosas))
                .setScale(ESCALA_MONTO, RoundingMode.HALF_UP);
        BigDecimal montoIva = subtotalComision.multiply(ivaPorcentaje).setScale(ESCALA_MONTO, RoundingMode.HALF_UP);
        BigDecimal totalDebitado = subtotalComision.add(montoIva).setScale(ESCALA_MONTO, RoundingMode.HALF_UP);
        return new CalculoLiquidacionInternalDto(
                transaccionesExitosas,
                transaccionesFallidas,
                tarifaServicio,
                tarifaUnitaria,
                ivaPorcentaje,
                subtotalComision,
                montoIva,
                totalDebitado
        );
    }

    private String normalizarTipoServicio(String tipoServicio) {
        if (tipoServicio == null || tipoServicio.isBlank()) {
            return null;
        }
        return tipoServicio;
    }

}
