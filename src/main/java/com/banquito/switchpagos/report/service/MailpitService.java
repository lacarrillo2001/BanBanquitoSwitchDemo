package com.banquito.switchpagos.report.service;

import com.fasterxml.jackson.databind.JsonNode;

public interface MailpitService {

    JsonNode listarMensajes();

    JsonNode obtenerMensaje(String id);

    void borrarTodosLosMensajes();
}
