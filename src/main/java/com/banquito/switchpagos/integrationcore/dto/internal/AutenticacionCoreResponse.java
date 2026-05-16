package com.banquito.switchpagos.integrationcore.dto.internal;

public record AutenticacionCoreResponse(
        Boolean autenticado,
        String tipoUsuario,
        Integer credencialWebId,
        Integer clienteId,
        String rucEmpresa,
        String usuario,
        String nombre,
        String rolSwitch,
        String estado,
        Boolean activoPagosMasivos
) {
}
