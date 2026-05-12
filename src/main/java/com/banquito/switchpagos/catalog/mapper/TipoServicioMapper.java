package com.banquito.switchpagos.catalog.mapper;

import com.banquito.switchpagos.catalog.dto.api.TipoServicioResponse;
import com.banquito.switchpagos.catalog.model.TipoServicio;
import org.springframework.stereotype.Component;

@Component
public class TipoServicioMapper {

    public TipoServicioResponse toResponse(TipoServicio tipoServicio) {
        String estado = tipoServicio.getEstado() != null ? tipoServicio.getEstado().name() : null;
        return new TipoServicioResponse(
                tipoServicio.getCodigo(),
                tipoServicio.getNombre(),
                tipoServicio.getDescripcion(),
                estado
        );
    }
}
