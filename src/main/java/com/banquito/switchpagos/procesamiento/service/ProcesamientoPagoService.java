package com.banquito.switchpagos.procesamiento.service;

import com.banquito.switchpagos.procesamiento.dto.api.ProcesarLoteRequest;
import com.banquito.switchpagos.procesamiento.dto.api.ProcesarLoteResponse;

import java.util.UUID;

public interface ProcesamientoPagoService {

    ProcesarLoteResponse procesarLote(UUID uuidLote, ProcesarLoteRequest procesarLoteRequest);
}
