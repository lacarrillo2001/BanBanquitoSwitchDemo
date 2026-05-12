package com.banquito.switchpagos.file.dto.internal;

import java.util.List;

public record ResultadoValidacionArchivoInternalDto(
        Boolean valido,
        List<ErrorValidacionArchivoInternalDto> errores
) {
}
