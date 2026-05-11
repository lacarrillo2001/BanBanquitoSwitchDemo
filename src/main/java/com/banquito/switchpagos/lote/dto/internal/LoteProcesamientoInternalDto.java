package com.banquito.switchpagos.lote.dto.internal;

import com.banquito.switchpagos.lote.enums.EstadoLote;

import java.time.OffsetDateTime;
import java.util.UUID;

public record LoteProcesamientoInternalDto(
        Long idLote,
        UUID uuidLote,
        String rucEmpresa,
        String tipoServicio,
        String cuentaMatrizCargo,
        EstadoLote estado,
        OffsetDateTime fechaInicioProceso,
        OffsetDateTime fechaFinProceso
) {
}
