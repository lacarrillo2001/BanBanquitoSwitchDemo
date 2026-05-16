package com.banquito.switchpagos.processing.service;

import com.banquito.switchpagos.processing.dto.api.ProcesarLoteRequest;
import com.banquito.switchpagos.processing.dto.api.ProcesarLoteResponse;

import java.util.UUID;

public interface ProcesamientoPagoService {

    ProcesarLoteResponse procesarLote(UUID uuidLote, ProcesarLoteRequest procesarLoteRequest);
}
