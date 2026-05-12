package com.banquito.switchpagos.integrationcore.service.impl;

import com.banquito.switchpagos.integrationcore.dto.internal.ConsultaSaldoCoreResponse;
import com.banquito.switchpagos.integrationcore.dto.internal.MovimientoCoreRequest;
import com.banquito.switchpagos.integrationcore.dto.internal.MovimientoCoreResponse;
import com.banquito.switchpagos.integrationcore.dto.internal.ValidacionCuentaCoreResponse;
import com.banquito.switchpagos.integrationcore.mapper.CoreBancarioMapper;
import com.banquito.switchpagos.integrationcore.service.CoreBancarioService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class CoreBancarioServiceStub implements CoreBancarioService {

    private static final BigDecimal SALDO_DISPONIBLE_SIMULADO = new BigDecimal("100000.00");
    private final CoreBancarioMapper coreBancarioMapper;

    public CoreBancarioServiceStub(CoreBancarioMapper coreBancarioMapper) {
        this.coreBancarioMapper = coreBancarioMapper;
    }

    @Override
    public ConsultaSaldoCoreResponse consultarSaldoDisponible(String numeroCuenta) {
        return coreBancarioMapper.toConsultaSaldoResponse(SALDO_DISPONIBLE_SIMULADO);
    }

    @Override
    public ValidacionCuentaCoreResponse validarCuentaDestino(String numeroCuenta, String identificacionBeneficiario) {
        if (numeroCuenta != null && numeroCuenta.endsWith("0000")) {
            return coreBancarioMapper.toValidacionCuentaResponse(
                    Boolean.FALSE,
                    "CUENTA_DESTINO_NO_EXISTE",
                    "La cuenta destino no existe en el Core Bancario simulado."
            );
        }
        if (numeroCuenta != null && numeroCuenta.endsWith("9999")) {
            return coreBancarioMapper.toValidacionCuentaResponse(
                    Boolean.FALSE,
                    "CUENTA_DESTINO_BLOQUEADA",
                    "La cuenta destino se encuentra bloqueada en el Core Bancario simulado."
            );
        }
        return coreBancarioMapper.toValidacionCuentaResponse(
                Boolean.TRUE,
                "CUENTA_DESTINO_VALIDA",
                "Cuenta destino valida en el Core Bancario simulado."
        );
    }

    @Override
    public MovimientoCoreResponse ejecutarDebito(MovimientoCoreRequest movimientoCoreRequest) {
        if (!Boolean.TRUE.equals(movimientoCoreRequest.permiteSobregiro())
                && movimientoCoreRequest.monto().compareTo(SALDO_DISPONIBLE_SIMULADO) > 0) {
            return coreBancarioMapper.toMovimientoResponse(
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
        return coreBancarioMapper.toMovimientoResponse(
                Boolean.TRUE,
                "DEBITO_CORE_EXITOSO",
                "Debito simulado ejecutado correctamente.",
                UUID.randomUUID(),
                uuidGrupoCore
        );
    }

    @Override
    public MovimientoCoreResponse ejecutarCredito(MovimientoCoreRequest movimientoCoreRequest) {
        return coreBancarioMapper.toMovimientoResponse(
                Boolean.TRUE,
                "CREDITO_CORE_EXITOSO",
                "Credito simulado ejecutado correctamente.",
                UUID.randomUUID(),
                movimientoCoreRequest.uuidGrupoCore() != null ? movimientoCoreRequest.uuidGrupoCore() : UUID.randomUUID()
        );
    }
}
