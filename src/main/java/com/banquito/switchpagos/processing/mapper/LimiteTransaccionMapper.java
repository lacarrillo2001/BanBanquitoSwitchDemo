package com.banquito.switchpagos.processing.mapper;

import com.banquito.switchpagos.processing.model.LimiteTransaccion;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.stereotype.Component;

@Component
public class LimiteTransaccionMapper {

    public ObjectNode toDatosBasicosNode(LimiteTransaccion limiteTransaccion, ObjectMapper objectMapper) {
        ObjectNode datos = objectMapper.createObjectNode();
        datos.put("idLimite", limiteTransaccion.getIdLimite());
        datos.put("tipoServicio", limiteTransaccion.getTipoServicio() != null
                ? limiteTransaccion.getTipoServicio().getCodigo()
                : null);
        datos.put("estado", limiteTransaccion.getEstado() != null ? limiteTransaccion.getEstado().name() : null);
        datos.put("moneda", limiteTransaccion.getMoneda());
        return datos;
    }
}
