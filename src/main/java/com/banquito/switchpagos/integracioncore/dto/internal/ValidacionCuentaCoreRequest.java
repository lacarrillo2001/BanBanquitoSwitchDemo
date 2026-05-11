package com.banquito.switchpagos.integracioncore.dto.internal;

public record ValidacionCuentaCoreRequest(
        String numeroCuenta,
        String identificacionBeneficiario
) {
}
