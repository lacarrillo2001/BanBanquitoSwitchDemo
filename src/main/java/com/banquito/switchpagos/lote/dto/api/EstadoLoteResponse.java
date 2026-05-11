package com.banquito.switchpagos.lote.dto.api;

import java.util.List;
import java.util.UUID;

public record EstadoLoteResponse(
        UUID uuidLote,
        String estado,
        String motivoRechazoGlobal,
        ResumenEstadoLoteResponse resumenLineas,
        FechasLoteResponse fechas,
        List<String> accionesDisponibles
) {
}
