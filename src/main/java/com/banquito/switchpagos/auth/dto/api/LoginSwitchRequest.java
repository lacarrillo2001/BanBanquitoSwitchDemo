package com.banquito.switchpagos.auth.dto.api;

public record LoginSwitchRequest(
        String usuario,
        String contrasena
) {
}
