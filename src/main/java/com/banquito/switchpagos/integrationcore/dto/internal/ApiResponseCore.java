package com.banquito.switchpagos.integrationcore.dto.internal;

public record ApiResponseCore<T>(
        Boolean success,
        String message,
        T data
) {
}
