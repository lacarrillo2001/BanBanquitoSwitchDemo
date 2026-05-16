package com.banquito.switchpagos.audit.mapper;

import com.banquito.switchpagos.audit.dto.internal.RegistroAuditoriaRequest;
import com.banquito.switchpagos.audit.model.BitacoraAuditoriaSwitch;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;

@Component
public class BitacoraAuditoriaSwitchMapper {

    public BitacoraAuditoriaSwitch toEntity(RegistroAuditoriaRequest registroAuditoriaRequest) {
        BitacoraAuditoriaSwitch bitacoraAuditoriaSwitch = new BitacoraAuditoriaSwitch();
        bitacoraAuditoriaSwitch.setTipoActor(registroAuditoriaRequest.getTipoActor());
        bitacoraAuditoriaSwitch.setIdActor(registroAuditoriaRequest.getIdActor());
        bitacoraAuditoriaSwitch.setRucEmpresa(registroAuditoriaRequest.getRucEmpresa());
        bitacoraAuditoriaSwitch.setAccion(registroAuditoriaRequest.getAccion());
        bitacoraAuditoriaSwitch.setEntidad(registroAuditoriaRequest.getEntidad());
        bitacoraAuditoriaSwitch.setIdEntidad(registroAuditoriaRequest.getIdEntidad());
        bitacoraAuditoriaSwitch.setDatosAntes(registroAuditoriaRequest.getDatosAntes());
        bitacoraAuditoriaSwitch.setDatosDespues(registroAuditoriaRequest.getDatosDespues());
        bitacoraAuditoriaSwitch.setDireccionIp(registroAuditoriaRequest.getDireccionIp());
        bitacoraAuditoriaSwitch.setAgenteUsuario(registroAuditoriaRequest.getAgenteUsuario());
        bitacoraAuditoriaSwitch.setFechaCreacion(OffsetDateTime.now());
        return bitacoraAuditoriaSwitch;
    }
}
