package com.banquito.switchpagos.archivo.dto.internal;

import java.util.List;

public record ResultadoValidacionArchivoInternalDto(
        Boolean valido,
        List<ErrorValidacionArchivoInternalDto> errores
) {
}
