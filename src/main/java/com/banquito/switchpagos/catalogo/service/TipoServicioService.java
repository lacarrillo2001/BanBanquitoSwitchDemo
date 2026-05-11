package com.banquito.switchpagos.catalogo.service;

import com.banquito.switchpagos.catalogo.dto.api.TipoServicioResponse;

import java.util.List;

public interface TipoServicioService {

    TipoServicioResponse obtenerPorCodigo(String codigo);

    Boolean existeActivo(String codigo);

    List<TipoServicioResponse> listarActivos();
}
