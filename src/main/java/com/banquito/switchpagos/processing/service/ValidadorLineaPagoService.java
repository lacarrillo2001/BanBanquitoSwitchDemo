package com.banquito.switchpagos.processing.service;

import com.banquito.switchpagos.processing.model.LineaPago;

public interface ValidadorLineaPagoService {

    void validarLimite(String tipoServicio, LineaPago lineaPago);
}
