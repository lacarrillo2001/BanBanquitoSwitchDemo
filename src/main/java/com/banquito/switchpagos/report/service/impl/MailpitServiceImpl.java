package com.banquito.switchpagos.report.service.impl;

import com.banquito.switchpagos.report.service.MailpitService;
import com.banquito.switchpagos.shared.exception.IntegracionCoreException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class MailpitServiceImpl implements MailpitService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final String apiUrl;

    public MailpitServiceImpl(@Value("${mailpit.api.url}") String apiUrl,
                              ObjectMapper objectMapper) {
        this.restTemplate = new RestTemplate();
        this.apiUrl = apiUrl;
        this.objectMapper = objectMapper;
    }

    @Override
    public JsonNode listarMensajes() {
        return leerRespuestaJson(apiUrl + "/messages");
    }

    @Override
    public JsonNode obtenerMensaje(String id) {
        return leerRespuestaJson(apiUrl + "/message/" + id);
    }

    @Override
    public void borrarTodosLosMensajes() {
        restTemplate.delete(apiUrl + "/messages");
    }

    private JsonNode leerRespuestaJson(String url) {
        try {
            String respuesta = restTemplate.getForObject(url, String.class);
            if (respuesta == null || respuesta.isBlank()) {
                return objectMapper.createObjectNode();
            }
            return objectMapper.readTree(respuesta);
        } catch (Exception exception) {
            throw new IntegracionCoreException(
                    "MAILPIT_RESPUESTA_INVALIDA",
                    "No fue posible interpretar la respuesta JSON de Mailpit.",
                    exception
            );
        }
    }
}
