package com.banquito.switchpagos.archivo.dto.internal;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record CabeceraArchivoPagoInternalDto(
        String rucEmpresa,
        String tipoServicio,
        OffsetDateTime fechaHoraGeneracion,
        String cuentaMatrizCargo,
        Integer totalRegistrosDeclarado,
        BigDecimal montoTotalDeclarado
) {
}
