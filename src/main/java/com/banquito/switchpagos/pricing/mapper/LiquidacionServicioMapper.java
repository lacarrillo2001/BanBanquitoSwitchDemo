package com.banquito.switchpagos.pricing.mapper;

import com.banquito.switchpagos.batch.model.LotePago;
import com.banquito.switchpagos.pricing.dto.api.LiquidarLoteResponse;
import com.banquito.switchpagos.pricing.dto.api.MovimientoContableResponse;
import com.banquito.switchpagos.pricing.dto.internal.CalculoLiquidacionInternalDto;
import com.banquito.switchpagos.pricing.dto.internal.LiquidacionComprobanteInternalDto;
import com.banquito.switchpagos.pricing.dto.internal.MovimientoContableInternalDto;
import com.banquito.switchpagos.pricing.enums.EstadoDebitoLiquidacion;
import com.banquito.switchpagos.pricing.model.LiquidacionServicio;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Component
public class LiquidacionServicioMapper {

    public LiquidacionServicio toPendienteEntity(LotePago lotePago, CalculoLiquidacionInternalDto calculo,
                                                 OffsetDateTime fechaCreacion) {
        LiquidacionServicio liquidacionServicio = new LiquidacionServicio();
        liquidacionServicio.setLotePago(lotePago);
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
        liquidacionServicio.setFechaCreacion(fechaCreacion);
        return liquidacionServicio;
    }

    public LiquidacionComprobanteInternalDto toComprobanteInternalDto(LiquidacionServicio liquidacionServicio) {
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

    public LiquidarLoteResponse toLiquidarLoteResponse(UUID uuidLote, LiquidacionServicio liquidacionServicio,
                                                       List<MovimientoContableInternalDto> movimientos) {
        List<MovimientoContableResponse> movimientosResponse = movimientos.stream()
                .map(this::toMovimientoResponse)
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

    private MovimientoContableResponse toMovimientoResponse(MovimientoContableInternalDto movimiento) {
        return new MovimientoContableResponse(
                movimiento.concepto().name(),
                movimiento.monto(),
                movimiento.estado()
        );
    }
}
