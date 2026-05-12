package com.banquito.switchpagos.catalog.service.impl;

import com.banquito.switchpagos.catalog.dto.api.TipoServicioResponse;
import com.banquito.switchpagos.catalog.enums.EstadoTipoServicio;
import com.banquito.switchpagos.catalog.mapper.TipoServicioMapper;
import com.banquito.switchpagos.catalog.model.TipoServicio;
import com.banquito.switchpagos.catalog.repository.TipoServicioRepository;
import com.banquito.switchpagos.catalog.service.TipoServicioService;
import com.banquito.switchpagos.shared.exception.RecursoNoEncontradoException;
import com.banquito.switchpagos.shared.exception.SolicitudInvalidaException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class TipoServicioServiceImpl implements TipoServicioService {

    private final TipoServicioRepository tipoServicioRepository;
    private final TipoServicioMapper tipoServicioMapper;

    public TipoServicioServiceImpl(TipoServicioRepository tipoServicioRepository,
                                   TipoServicioMapper tipoServicioMapper) {
        this.tipoServicioRepository = tipoServicioRepository;
        this.tipoServicioMapper = tipoServicioMapper;
    }

    @Override
    public TipoServicioResponse obtenerPorCodigo(String codigo) {
        validarCodigo(codigo);
        TipoServicio tipoServicio = tipoServicioRepository.findByCodigo(codigo)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "TIPO_SERVICIO_NO_ENCONTRADO",
                        "No existe un tipo de servicio con el codigo " + codigo + "."
                ));
        return tipoServicioMapper.toResponse(tipoServicio);
    }

    @Override
    public TipoServicio obtenerEntidadActiva(String codigo) {
        validarCodigo(codigo);
        TipoServicio tipoServicio = tipoServicioRepository.findByCodigo(codigo)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "TIPO_SERVICIO_NO_ENCONTRADO",
                        "No existe un tipo de servicio con el codigo " + codigo + "."
                ));
        if (!EstadoTipoServicio.ACTIVO.equals(tipoServicio.getEstado())) {
            throw new RecursoNoEncontradoException(
                    "TIPO_SERVICIO_INACTIVO",
                    "El tipo de servicio " + codigo + " no esta activo."
            );
        }
        return tipoServicio;
    }

    @Override
    public Boolean existeActivo(String codigo) {
        validarCodigo(codigo);
        return tipoServicioRepository.existsByCodigoAndEstado(codigo, EstadoTipoServicio.ACTIVO);
    }

    @Override
    public List<TipoServicioResponse> listarActivos() {
        return tipoServicioRepository.findByEstado(EstadoTipoServicio.ACTIVO)
                .stream()
                .map(tipoServicioMapper::toResponse)
                .toList();
    }

    private void validarCodigo(String codigo) {
        if (codigo == null || codigo.isBlank()) {
            throw new SolicitudInvalidaException(
                    "TIPO_SERVICIO_CODIGO_REQUERIDO",
                    "El codigo del tipo de servicio es obligatorio."
            );
        }
    }

}
