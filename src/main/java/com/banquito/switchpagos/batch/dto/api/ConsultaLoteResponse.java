package com.banquito.switchpagos.batch.dto.api;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record ConsultaLoteResponse(
        UUID uuidLote,
        String rucEmpresa,
        String tipoServicio,
        String nombreArchivo,
        String canalIngreso,
        String estado,
        Integer totalRegistrosDeclarado,
        BigDecimal montoTotalDeclarado,
        OffsetDateTime fechaRecepcion
) {
}
