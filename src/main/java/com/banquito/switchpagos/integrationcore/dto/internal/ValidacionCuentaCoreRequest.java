package com.banquito.switchpagos.integrationcore.dto.internal;

public record ValidacionCuentaCoreRequest(
        String numeroCuenta,
        String identificacionBeneficiario
) {
}
