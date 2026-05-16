package com.banquito.switchpagos.integrationcore.dto.internal;

public record ErrorCoreResponse(
        Boolean success,
        String code,
        String message,
        String path
) {
}
