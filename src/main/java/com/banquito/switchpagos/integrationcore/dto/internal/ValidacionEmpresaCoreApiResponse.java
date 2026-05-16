package com.banquito.switchpagos.integrationcore.dto.internal;

public record ValidacionEmpresaCoreApiResponse(
        String ruc,
        Boolean existe,
        String tipoCliente,
        String estado,
        Boolean activoPagosMasivos,
        Boolean credencialWebValida,
        Boolean habilitada,
        String codigo,
        String mensaje
) {
}
