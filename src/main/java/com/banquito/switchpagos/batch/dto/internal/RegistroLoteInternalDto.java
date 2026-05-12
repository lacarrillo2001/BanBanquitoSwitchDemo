package com.banquito.switchpagos.batch.dto.internal;

import com.banquito.switchpagos.batch.enums.CanalIngreso;
import org.springframework.web.multipart.MultipartFile;

public record RegistroLoteInternalDto(
        MultipartFile archivo,
        String tipoServicio,
        String cuentaMatrizCargo,
        CanalIngreso canalIngreso,
        Integer idCredencialWebCore,
        String rucEmpresa
) {
}
