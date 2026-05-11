package com.banquito.switchpagos.auditoria.service;

import com.banquito.switchpagos.auditoria.dto.internal.RegistroAuditoriaRequest;

public interface AuditoriaSwitchService {

    void registrarAccion(RegistroAuditoriaRequest registroAuditoriaRequest);
}
