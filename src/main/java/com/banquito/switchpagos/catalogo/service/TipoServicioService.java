package com.banquito.switchpagos.catalogo.service;

import com.banquito.switchpagos.catalogo.dto.api.TipoServicioResponse;
import com.banquito.switchpagos.catalogo.model.TipoServicio;

import java.util.List;

public interface TipoServicioService {

    TipoServicioResponse obtenerPorCodigo(String codigo);

    TipoServicio obtenerEntidadPorCodigo(String codigo);

    Boolean existeActivo(String codigo);

    List<TipoServicioResponse> listarActivos();
}
