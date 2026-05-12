package com.banquito.switchpagos.catalog.service;

import com.banquito.switchpagos.catalog.dto.api.TipoServicioResponse;
import com.banquito.switchpagos.catalog.model.TipoServicio;

import java.util.List;

public interface TipoServicioService {

    TipoServicioResponse obtenerPorCodigo(String codigo);

    TipoServicio obtenerEntidadActiva(String codigo);

    Boolean existeActivo(String codigo);

    List<TipoServicioResponse> listarActivos();
}
