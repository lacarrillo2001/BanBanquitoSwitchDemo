package com.banquito.switchpagos.integracioncore.service;

import com.banquito.switchpagos.integracioncore.dto.internal.ConsultaSaldoCoreResponse;
import com.banquito.switchpagos.integracioncore.dto.internal.MovimientoCoreRequest;
import com.banquito.switchpagos.integracioncore.dto.internal.MovimientoCoreResponse;
import com.banquito.switchpagos.integracioncore.dto.internal.ValidacionCuentaCoreResponse;

public interface CoreBancarioService {

    ConsultaSaldoCoreResponse consultarSaldoDisponible(String numeroCuenta);

    ValidacionCuentaCoreResponse validarCuentaDestino(String numeroCuenta, String identificacionBeneficiario);

    MovimientoCoreResponse ejecutarDebito(MovimientoCoreRequest movimientoCoreRequest);

    MovimientoCoreResponse ejecutarCredito(MovimientoCoreRequest movimientoCoreRequest);
}
