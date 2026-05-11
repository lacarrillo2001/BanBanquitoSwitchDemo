package com.banquito.switchpagos.reporte.service;

import com.banquito.switchpagos.procesamiento.dto.internal.LineaPagoInternalDto;

import java.util.UUID;

public interface NotificacionService {

    void registrarNotificacionesBeneficiarios(UUID uuidLote);

    void registrarNotificacionLineaExitosa(LineaPagoInternalDto lineaPagoInternalDto, String rucEmpresa);

    void enviarNotificacionesPendientes();
}
