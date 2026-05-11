package com.banquito.switchpagos.lote.dto.internal;

import com.banquito.switchpagos.lote.enums.CanalIngreso;
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
