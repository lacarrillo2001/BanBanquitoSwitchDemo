package com.banquito.switchpagos.procesamiento.service;

import com.banquito.switchpagos.procesamiento.model.LineaPago;

public interface ValidadorLineaPagoService {

    void validarLimite(String tipoServicio, LineaPago lineaPago);
}
