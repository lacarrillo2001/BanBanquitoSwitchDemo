package com.banquito.switchpagos.integrationcore.service;

import com.banquito.switchpagos.integrationcore.dto.internal.ConsultaSaldoCoreResponse;
import com.banquito.switchpagos.integrationcore.dto.internal.MovimientoCoreRequest;
import com.banquito.switchpagos.integrationcore.dto.internal.MovimientoCoreResponse;
import com.banquito.switchpagos.integrationcore.dto.internal.ValidacionCuentaCoreResponse;

public interface CoreBancarioService {

    ConsultaSaldoCoreResponse consultarSaldoDisponible(String numeroCuenta);

    ValidacionCuentaCoreResponse validarCuentaDestino(String numeroCuenta, String identificacionBeneficiario);

    MovimientoCoreResponse ejecutarDebito(MovimientoCoreRequest movimientoCoreRequest);

    MovimientoCoreResponse ejecutarCredito(MovimientoCoreRequest movimientoCoreRequest);
}
