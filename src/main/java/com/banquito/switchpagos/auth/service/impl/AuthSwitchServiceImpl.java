package com.banquito.switchpagos.auth.service.impl;

import com.banquito.switchpagos.auth.dto.api.LoginSwitchRequest;
import com.banquito.switchpagos.auth.dto.api.LoginSwitchResponse;
import com.banquito.switchpagos.auth.service.AuthSwitchService;
import com.banquito.switchpagos.integrationcore.dto.internal.AutenticacionCoreResponse;
import com.banquito.switchpagos.integrationcore.service.CoreBancarioService;
import com.banquito.switchpagos.shared.exception.SolicitudInvalidaException;
import org.springframework.stereotype.Service;

@Service
public class AuthSwitchServiceImpl implements AuthSwitchService {

    public static final String ROL_EMPRESA_PAGOS_MASIVOS = "EMPRESA_PAGOS_MASIVOS";

    private final CoreBancarioService coreBancarioService;

    public AuthSwitchServiceImpl(CoreBancarioService coreBancarioService) {
        this.coreBancarioService = coreBancarioService;
    }

    @Override
    public LoginSwitchResponse login(LoginSwitchRequest request) {
        validarRequest(request);
        AutenticacionCoreResponse autenticacion = coreBancarioService.autenticar(
                request.usuario(),
                request.contrasena()
        );
        validarAutenticacion(autenticacion);
        return new LoginSwitchResponse(
                autenticacion.autenticado(),
                autenticacion.tipoUsuario(),
                autenticacion.credencialWebId(),
                autenticacion.clienteId(),
                autenticacion.rucEmpresa(),
                autenticacion.usuario(),
                autenticacion.nombre(),
                autenticacion.rolSwitch(),
                autenticacion.estado(),
                autenticacion.activoPagosMasivos()
        );
    }

    private void validarRequest(LoginSwitchRequest request) {
        if (request == null || request.usuario() == null || request.usuario().isBlank()
                || request.contrasena() == null || request.contrasena().isBlank()) {
            throw new SolicitudInvalidaException(
                    "CREDENCIALES_REQUERIDAS",
                    "Usuario y contrasena son obligatorios."
            );
        }
    }

    private void validarAutenticacion(AutenticacionCoreResponse autenticacion) {
        if (autenticacion == null || !Boolean.TRUE.equals(autenticacion.autenticado())) {
            throw new SolicitudInvalidaException(
                    "LOGIN_INVALIDO",
                    "Usuario o contrasena invalidos."
            );
        }
        if (!ROL_EMPRESA_PAGOS_MASIVOS.equals(autenticacion.rolSwitch())) {
            throw new SolicitudInvalidaException(
                    "ROL_SWITCH_NO_AUTORIZADO",
                    "El usuario no tiene rol EMPRESA_PAGOS_MASIVOS."
            );
        }
        if (!Boolean.TRUE.equals(autenticacion.activoPagosMasivos())) {
            throw new SolicitudInvalidaException(
                    "EMPRESA_SIN_PAGOS_MASIVOS",
                    "La empresa no tiene activo el servicio de pagos masivos."
            );
        }
    }
}
