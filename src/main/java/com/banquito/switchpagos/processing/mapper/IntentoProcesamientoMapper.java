package com.banquito.switchpagos.processing.mapper;

import com.banquito.switchpagos.processing.model.IntentoProcesamiento;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.stereotype.Component;

@Component
public class IntentoProcesamientoMapper {

    public ObjectNode toDatosBasicosNode(IntentoProcesamiento intentoProcesamiento, ObjectMapper objectMapper) {
        ObjectNode datos = objectMapper.createObjectNode();
        datos.put("idIntento", intentoProcesamiento.getIdIntento());
        datos.put("numeroIntento", intentoProcesamiento.getNumeroIntento());
        datos.put("estado", intentoProcesamiento.getEstado() != null ? intentoProcesamiento.getEstado().name() : null);
        datos.put("codigoError", intentoProcesamiento.getCodigoError());
        return datos;
    }
}
