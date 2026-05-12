package com.banquito.switchpagos.auditoria.service.impl;

import com.banquito.switchpagos.auditoria.dto.internal.RegistroAuditoriaRequest;
import com.banquito.switchpagos.auditoria.model.BitacoraAuditoriaSwitch;
import com.banquito.switchpagos.auditoria.repository.BitacoraAuditoriaSwitchRepository;
import com.banquito.switchpagos.auditoria.service.AuditoriaSwitchService;
import com.banquito.switchpagos.common.exception.SolicitudInvalidaException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.OffsetDateTime;

@Service
public class AuditoriaSwitchServiceImpl implements AuditoriaSwitchService {

    private final BitacoraAuditoriaSwitchRepository bitacoraAuditoriaSwitchRepository;

    public AuditoriaSwitchServiceImpl(BitacoraAuditoriaSwitchRepository bitacoraAuditoriaSwitchRepository) {
        this.bitacoraAuditoriaSwitchRepository = bitacoraAuditoriaSwitchRepository;
    }

    @Override
    @Transactional
    public void registrarAccion(RegistroAuditoriaRequest registroAuditoriaRequest) {
        validarRegistro(registroAuditoriaRequest);

        BitacoraAuditoriaSwitch bitacoraAuditoriaSwitch = new BitacoraAuditoriaSwitch();
        bitacoraAuditoriaSwitch.setTipoActor(registroAuditoriaRequest.getTipoActor());
        bitacoraAuditoriaSwitch.setIdActor(registroAuditoriaRequest.getIdActor());
        bitacoraAuditoriaSwitch.setRucEmpresa(registroAuditoriaRequest.getRucEmpresa());
        bitacoraAuditoriaSwitch.setAccion(registroAuditoriaRequest.getAccion());
        bitacoraAuditoriaSwitch.setEntidad(registroAuditoriaRequest.getEntidad());
        bitacoraAuditoriaSwitch.setIdEntidad(registroAuditoriaRequest.getIdEntidad());
        bitacoraAuditoriaSwitch.setDatosAntes(registroAuditoriaRequest.getDatosAntes());
        bitacoraAuditoriaSwitch.setDatosDespues(registroAuditoriaRequest.getDatosDespues());
        bitacoraAuditoriaSwitch.setDireccionIp(resolverDireccionIp(registroAuditoriaRequest.getDireccionIp()));
        bitacoraAuditoriaSwitch.setAgenteUsuario(registroAuditoriaRequest.getAgenteUsuario());
        bitacoraAuditoriaSwitch.setFechaCreacion(OffsetDateTime.now());

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

    private InetAddress resolverDireccionIp(String direccionIp) {
        if (direccionIp == null || direccionIp.isBlank()) {
            return null;
        }
        try {
            return InetAddress.getByName(direccionIp.trim());
        } catch (UnknownHostException exception) {
            throw new SolicitudInvalidaException(
                    "AUDITORIA_DIRECCION_IP_INVALIDA",
                    "La direccion IP de auditoria no tiene un formato valido.",
                    exception
            );
        }
    }
}
