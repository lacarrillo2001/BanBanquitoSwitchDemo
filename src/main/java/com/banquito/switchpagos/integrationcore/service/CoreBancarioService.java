package com.banquito.switchpagos.integrationcore.service;

import com.banquito.switchpagos.integrationcore.dto.internal.AutenticacionCoreResponse;
import com.banquito.switchpagos.integrationcore.dto.internal.ConsultaSaldoCoreResponse;
import com.banquito.switchpagos.integrationcore.dto.internal.DiaHabilCoreResponse;
import com.banquito.switchpagos.integrationcore.dto.internal.LiquidacionCoreRequest;
import com.banquito.switchpagos.integrationcore.dto.internal.LiquidacionCoreResponse;
import com.banquito.switchpagos.integrationcore.dto.internal.MovimientoCoreRequest;
import com.banquito.switchpagos.integrationcore.dto.internal.MovimientoCoreResponse;
import com.banquito.switchpagos.integrationcore.dto.internal.CuentaFavoritaPagosCoreResponse;
import com.banquito.switchpagos.integrationcore.dto.internal.ValidacionCoreResponse;
import com.banquito.switchpagos.integrationcore.dto.internal.ValidacionCuentaCoreResponse;

import java.time.LocalDate;

public interface CoreBancarioService {

    AutenticacionCoreResponse autenticar(String usuario, String contrasena);

    ValidacionCoreResponse validarEmpresa(String ruc);

    ValidacionCoreResponse validarCuentaMatriz(String ruc, String numeroCuenta);

    CuentaFavoritaPagosCoreResponse obtenerCuentaFavoritaPagos(String ruc);

    ValidacionCoreResponse validarCredencialEmpresa(String ruc, String username);

    DiaHabilCoreResponse consultarDiaHabil(LocalDate fecha);

    ConsultaSaldoCoreResponse consultarSaldoDisponible(String numeroCuenta);

    ValidacionCuentaCoreResponse validarCuentaDestino(String numeroCuenta, String identificacionBeneficiario);

    MovimientoCoreResponse ejecutarDebito(MovimientoCoreRequest movimientoCoreRequest);

    MovimientoCoreResponse ejecutarCredito(MovimientoCoreRequest movimientoCoreRequest);

    LiquidacionCoreResponse liquidarServicio(LiquidacionCoreRequest liquidacionCoreRequest);
}
