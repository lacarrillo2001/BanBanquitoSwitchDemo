package com.banquito.switchpagos.pricing.mapper;

import com.banquito.switchpagos.pricing.dto.internal.MovimientoContableInternalDto;
import com.banquito.switchpagos.pricing.model.DetalleLiquidacion;
import com.banquito.switchpagos.pricing.model.LiquidacionServicio;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;

@Component
public class DetalleLiquidacionMapper {

    public DetalleLiquidacion toEntity(LiquidacionServicio liquidacionServicio,
                                       MovimientoContableInternalDto movimientoContableInternalDto,
                                       OffsetDateTime fechaCreacion) {
        DetalleLiquidacion detalleLiquidacion = new DetalleLiquidacion();
        detalleLiquidacion.setLiquidacionServicio(liquidacionServicio);
        detalleLiquidacion.setConcepto(movimientoContableInternalDto.concepto());
        detalleLiquidacion.setMonto(movimientoContableInternalDto.monto());
        detalleLiquidacion.setUuidTransaccionCore(movimientoContableInternalDto.uuidTransaccionCore());
        detalleLiquidacion.setCuentaOrigenCore(movimientoContableInternalDto.cuentaOrigenCore());
        detalleLiquidacion.setCuentaDestinoCore(movimientoContableInternalDto.cuentaDestinoCore());
        detalleLiquidacion.setFechaCreacion(fechaCreacion);
        return detalleLiquidacion;
    }
}
