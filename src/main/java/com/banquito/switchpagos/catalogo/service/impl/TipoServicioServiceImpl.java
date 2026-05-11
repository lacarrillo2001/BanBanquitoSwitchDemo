package com.banquito.switchpagos.catalogo.service.impl;

import com.banquito.switchpagos.catalogo.dto.api.TipoServicioResponse;
import com.banquito.switchpagos.catalogo.enums.EstadoTipoServicio;
import com.banquito.switchpagos.catalogo.model.TipoServicio;
import com.banquito.switchpagos.catalogo.repository.TipoServicioRepository;
import com.banquito.switchpagos.catalogo.service.TipoServicioService;
import com.banquito.switchpagos.common.exception.RecursoNoEncontradoException;
import com.banquito.switchpagos.common.exception.SolicitudInvalidaException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class TipoServicioServiceImpl implements TipoServicioService {

    private final TipoServicioRepository tipoServicioRepository;

    public TipoServicioServiceImpl(TipoServicioRepository tipoServicioRepository) {
        this.tipoServicioRepository = tipoServicioRepository;
    }

    @Override
    public TipoServicioResponse obtenerPorCodigo(String codigo) {
        validarCodigo(codigo);
        TipoServicio tipoServicio = tipoServicioRepository.findByCodigo(codigo)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "TIPO_SERVICIO_NO_ENCONTRADO",
                        "No existe un tipo de servicio con el codigo " + codigo + "."
                ));
        return construirResponse(tipoServicio);
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
                .map(this::construirResponse)
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

    private TipoServicioResponse construirResponse(TipoServicio tipoServicio) {
        String estado = tipoServicio.getEstado() != null ? tipoServicio.getEstado().name() : null;
        return new TipoServicioResponse(
                tipoServicio.getCodigo(),
                tipoServicio.getNombre(),
                tipoServicio.getDescripcion(),
                estado
        );
    }
}
