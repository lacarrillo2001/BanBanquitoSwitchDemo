package com.banquito.switchpagos.parameter.mapper;

import com.banquito.switchpagos.parameter.model.ParametroSwitch;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.stereotype.Component;

@Component
public class ParametroSwitchMapper {

    public ObjectNode toDatosBasicosNode(ParametroSwitch parametroSwitch, ObjectMapper objectMapper) {
        ObjectNode datos = objectMapper.createObjectNode();
        datos.put("codigo", parametroSwitch.getCodigo());
        datos.put("nombre", parametroSwitch.getNombre());
        datos.put("tipoDato", parametroSwitch.getTipoDato() != null ? parametroSwitch.getTipoDato().name() : null);
        return datos;
    }
}
