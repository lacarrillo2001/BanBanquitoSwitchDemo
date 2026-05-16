package com.banquito.switchpagos.integrationcore.dto.internal;

public record ValidacionCredencialEmpresaCoreApiResponse(
        String ruc,
        String username,
        Boolean existe,
        Boolean perteneceEmpresa,
        String estado,
        Boolean valida,
        String codigo,
        String mensaje
) {
}
