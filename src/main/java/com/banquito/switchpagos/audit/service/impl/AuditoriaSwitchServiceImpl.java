package com.banquito.switchpagos.audit.service.impl;

import com.banquito.switchpagos.audit.dto.internal.RegistroAuditoriaRequest;
import com.banquito.switchpagos.audit.mapper.BitacoraAuditoriaSwitchMapper;
import com.banquito.switchpagos.audit.model.BitacoraAuditoriaSwitch;
import com.banquito.switchpagos.audit.repository.BitacoraAuditoriaSwitchRepository;
import com.banquito.switchpagos.audit.service.AuditoriaSwitchService;
import com.banquito.switchpagos.shared.exception.SolicitudInvalidaException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuditoriaSwitchServiceImpl implements AuditoriaSwitchService {

    private final BitacoraAuditoriaSwitchRepository bitacoraAuditoriaSwitchRepository;
    private final BitacoraAuditoriaSwitchMapper bitacoraAuditoriaSwitchMapper;

    public AuditoriaSwitchServiceImpl(BitacoraAuditoriaSwitchRepository bitacoraAuditoriaSwitchRepository,
                                      BitacoraAuditoriaSwitchMapper bitacoraAuditoriaSwitchMapper) {
        this.bitacoraAuditoriaSwitchRepository = bitacoraAuditoriaSwitchRepository;
        this.bitacoraAuditoriaSwitchMapper = bitacoraAuditoriaSwitchMapper;
    }

    @Override
    @Transactional
    public void registrarAccion(RegistroAuditoriaRequest registroAuditoriaRequest) {
        validarRegistro(registroAuditoriaRequest);

        BitacoraAuditoriaSwitch bitacoraAuditoriaSwitch = bitacoraAuditoriaSwitchMapper.toEntity(registroAuditoriaRequest);
        bitacoraAuditoriaSwitchRepository.save(bitacoraAuditoriaSwitch);
    }

    private void validarRegistro(RegistroAuditoriaRequest registroAuditoriaRequest) {
        if (registroAuditoriaRequest == null) {
            throw new SolicitudInvalidaException(
                    "AUDITORIA_REGISTRO_REQUERIDO",
                    "Los datos de auditoria son obligatorios."
            );
        }
        if (registroAuditoriaRequest.getTipoActor() == null) {
            throw new SolicitudInvalidaException(
                    "AUDITORIA_TIPO_ACTOR_REQUERIDO",
                    "El tipo de actor de auditoria es obligatorio."
            );
        }
        if (registroAuditoriaRequest.getAccion() == null || registroAuditoriaRequest.getAccion().isBlank()) {
            throw new SolicitudInvalidaException(
                    "AUDITORIA_ACCION_REQUERIDA",
                    "La accion de auditoria es obligatoria."
            );
        }
        if (registroAuditoriaRequest.getEntidad() == null || registroAuditoriaRequest.getEntidad().isBlank()) {
            throw new SolicitudInvalidaException(
                    "AUDITORIA_ENTIDAD_REQUERIDA",
                    "La entidad auditada es obligatoria."
            );
        }
    }
}
