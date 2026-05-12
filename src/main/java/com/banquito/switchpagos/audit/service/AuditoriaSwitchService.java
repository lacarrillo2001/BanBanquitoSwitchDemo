package com.banquito.switchpagos.audit.service;

import com.banquito.switchpagos.audit.dto.internal.RegistroAuditoriaRequest;

public interface AuditoriaSwitchService {

    void registrarAccion(RegistroAuditoriaRequest registroAuditoriaRequest);
}
