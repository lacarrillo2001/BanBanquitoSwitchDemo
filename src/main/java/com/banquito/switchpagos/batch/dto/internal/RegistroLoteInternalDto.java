package com.banquito.switchpagos.batch.dto.internal;

import com.banquito.switchpagos.batch.enums.CanalIngreso;
import org.springframework.web.multipart.MultipartFile;
import java.nio.file.Path;

public record RegistroLoteInternalDto(
        MultipartFile archivo,
        Path rutaArchivo,
        String tipoServicio,
        String cuentaMatrizCargo,
        CanalIngreso canalIngreso,
        Integer idCredencialWebCore,
        String rucEmpresa
) {
}
