package com.banquito.switchpagos.auth.dto.api;

public record LoginSwitchResponse(
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
