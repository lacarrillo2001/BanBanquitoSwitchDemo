package com.banquito.switchpagos.batch.service;

import com.banquito.switchpagos.batch.dto.api.ProcesarPendientesColaResponse;

public interface ColaProcesamientoService {

    ProcesarPendientesColaResponse procesarPendientesVencidos();

    ProcesarPendientesColaResponse procesarPendientesManual();
}
