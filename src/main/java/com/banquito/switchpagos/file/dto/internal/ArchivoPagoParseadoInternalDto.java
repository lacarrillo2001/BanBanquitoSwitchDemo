package com.banquito.switchpagos.file.dto.internal;

import java.util.List;

public record ArchivoPagoParseadoInternalDto(
        String nombreArchivo,
        String hashArchivo,
        Long tamanoBytes,
        CabeceraArchivoPagoInternalDto cabecera,
        List<DetalleArchivoPagoInternalDto> detalles,
        PieArchivoPagoInternalDto pie
) {
}
