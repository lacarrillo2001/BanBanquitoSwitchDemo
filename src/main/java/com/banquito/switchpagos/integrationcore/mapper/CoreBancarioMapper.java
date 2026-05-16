package com.banquito.switchpagos.integrationcore.mapper;

import com.banquito.switchpagos.integrationcore.dto.internal.ConsultaSaldoCoreResponse;
import com.banquito.switchpagos.integrationcore.dto.internal.MovimientoCoreResponse;
import com.banquito.switchpagos.integrationcore.dto.internal.ValidacionCuentaCoreResponse;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

@Component
public class CoreBancarioMapper {

    public ConsultaSaldoCoreResponse toConsultaSaldoResponse(BigDecimal saldoDisponible) {
        return new ConsultaSaldoCoreResponse(
                Boolean.TRUE,
                "SALDO_CONSULTADO",
                "Saldo disponible simulado consultado correctamente.",
                saldoDisponible
        );
    }

    public ValidacionCuentaCoreResponse toValidacionCuentaResponse(Boolean valida, String codigo, String mensaje) {
        return new ValidacionCuentaCoreResponse(valida, codigo, mensaje);
    }

    public MovimientoCoreResponse toMovimientoResponse(Boolean exitoso, String codigo, String mensaje,
                                                       UUID uuidTransaccionCore, UUID uuidGrupoCore) {
        return new MovimientoCoreResponse(exitoso, codigo, mensaje, uuidTransaccionCore, uuidGrupoCore);
    }
}
