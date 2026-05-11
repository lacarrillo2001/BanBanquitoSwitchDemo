package com.banquito.switchpagos.integracioncore.service.impl;

import com.banquito.switchpagos.integracioncore.dto.internal.ConsultaSaldoCoreResponse;
import com.banquito.switchpagos.integracioncore.dto.internal.MovimientoCoreRequest;
import com.banquito.switchpagos.integracioncore.dto.internal.MovimientoCoreResponse;
import com.banquito.switchpagos.integracioncore.dto.internal.ValidacionCuentaCoreResponse;
import com.banquito.switchpagos.integracioncore.service.CoreBancarioService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class CoreBancarioServiceStub implements CoreBancarioService {

    private static final BigDecimal SALDO_DISPONIBLE_SIMULADO = new BigDecimal("100000.00");

    @Override
    public ConsultaSaldoCoreResponse consultarSaldoDisponible(String numeroCuenta) {
        return new ConsultaSaldoCoreResponse(
                Boolean.TRUE,
                "SALDO_CONSULTADO",
                "Saldo disponible simulado consultado correctamente.",
                SALDO_DISPONIBLE_SIMULADO
        );
    }

    @Override
    public ValidacionCuentaCoreResponse validarCuentaDestino(String numeroCuenta, String identificacionBeneficiario) {
        if (numeroCuenta != null && numeroCuenta.endsWith("0000")) {
            return new ValidacionCuentaCoreResponse(
                    Boolean.FALSE,
                    "CUENTA_DESTINO_NO_EXISTE",
                    "La cuenta destino no existe en el Core Bancario simulado."
            );
        }
        if (numeroCuenta != null && numeroCuenta.endsWith("9999")) {
            return new ValidacionCuentaCoreResponse(
                    Boolean.FALSE,
                    "CUENTA_DESTINO_BLOQUEADA",
                    "La cuenta destino se encuentra bloqueada en el Core Bancario simulado."
            );
        }
        return new ValidacionCuentaCoreResponse(
                Boolean.TRUE,
                "CUENTA_DESTINO_VALIDA",
                "Cuenta destino valida en el Core Bancario simulado."
        );
    }

    @Override
    public MovimientoCoreResponse ejecutarDebito(MovimientoCoreRequest movimientoCoreRequest) {
        if (!Boolean.TRUE.equals(movimientoCoreRequest.permiteSobregiro())
                && movimientoCoreRequest.monto().compareTo(SALDO_DISPONIBLE_SIMULADO) > 0) {
            return new MovimientoCoreResponse(
                    Boolean.FALSE,
                    "SALDO_INSUFICIENTE",
                    "La cuenta matriz no tiene saldo suficiente en el Core Bancario simulado.",
                    null,
                    movimientoCoreRequest.uuidGrupoCore()
            );
        }
        UUID uuidGrupoCore = movimientoCoreRequest.uuidGrupoCore() != null
                ? movimientoCoreRequest.uuidGrupoCore()
                : UUID.randomUUID();
        return new MovimientoCoreResponse(
                Boolean.TRUE,
                "DEBITO_CORE_EXITOSO",
                "Debito simulado ejecutado correctamente.",
                UUID.randomUUID(),
                uuidGrupoCore
        );
    }

    @Override
    public MovimientoCoreResponse ejecutarCredito(MovimientoCoreRequest movimientoCoreRequest) {
        return new MovimientoCoreResponse(
                Boolean.TRUE,
                "CREDITO_CORE_EXITOSO",
                "Credito simulado ejecutado correctamente.",
                UUID.randomUUID(),
                movimientoCoreRequest.uuidGrupoCore() != null ? movimientoCoreRequest.uuidGrupoCore() : UUID.randomUUID()
        );
    }
}
