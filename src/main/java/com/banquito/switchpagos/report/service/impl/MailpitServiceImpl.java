package com.banquito.switchpagos.report.service.impl;

import com.banquito.switchpagos.report.service.MailpitService;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class MailpitServiceImpl implements MailpitService {

    private final RestTemplate restTemplate;
    private final String apiUrl;

    public MailpitServiceImpl(@Value("${mailpit.api.url}") String apiUrl) {
        this.restTemplate = new RestTemplate();
        this.apiUrl = apiUrl;
    }

    @Override
    public JsonNode listarMensajes() {
        return restTemplate.getForObject(apiUrl + "/messages", JsonNode.class);
    }

    @Override
    public JsonNode obtenerMensaje(String id) {
        return restTemplate.getForObject(apiUrl + "/message/" + id, JsonNode.class);
    }

    @Override
    public void borrarTodosLosMensajes() {
        restTemplate.delete(apiUrl + "/messages");
    }
}
