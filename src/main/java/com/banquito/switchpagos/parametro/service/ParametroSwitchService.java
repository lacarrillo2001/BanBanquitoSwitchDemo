package com.banquito.switchpagos.parametro.service;

import com.banquito.switchpagos.parametro.model.ParametroSwitch;

import java.math.BigDecimal;
import java.time.LocalTime;

public interface ParametroSwitchService {

    String obtenerValorTexto(String codigo);

    BigDecimal obtenerDecimal(String codigo);

    Integer obtenerInteger(String codigo);

    LocalTime obtenerHora(String codigo);

    Boolean obtenerBoolean(String codigo);

    ParametroSwitch obtenerParametro(String codigo);
}
