package com.banquito.switchpagos.report.service;

import com.banquito.switchpagos.processing.dto.internal.LineaPagoInternalDto;

import java.util.UUID;

public interface NotificacionService {

    void registrarNotificacionesBeneficiarios(UUID uuidLote);

    void registrarNotificacionLineaExitosa(LineaPagoInternalDto lineaPagoInternalDto, String rucEmpresa);

    void enviarNotificacionesPendientes();

    void enviarEmailPruebaDirecto(String destinatario, String asunto, String cuerpo);

    String obtenerResumenDb();
}
