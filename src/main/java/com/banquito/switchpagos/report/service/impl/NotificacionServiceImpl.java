package com.banquito.switchpagos.report.service.impl;

import com.banquito.switchpagos.audit.dto.internal.RegistroAuditoriaRequest;
import com.banquito.switchpagos.audit.enums.TipoActorAuditoria;
import com.banquito.switchpagos.audit.service.AuditoriaSwitchService;
import com.banquito.switchpagos.batch.service.LotePagoService;
import com.banquito.switchpagos.processing.dto.internal.LineaPagoInternalDto;
import com.banquito.switchpagos.processing.enums.EstadoLineaPago;
import com.banquito.switchpagos.processing.model.LineaPago;
import com.banquito.switchpagos.processing.service.LineaPagoService;
import com.banquito.switchpagos.report.enums.EstadoEnvioNotificacion;
import com.banquito.switchpagos.report.mapper.NotificacionBeneficiarioMapper;
import com.banquito.switchpagos.report.model.NotificacionBeneficiario;
import com.banquito.switchpagos.report.repository.NotificacionBeneficiarioRepository;
import com.banquito.switchpagos.report.service.NotificacionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.UUID;

@Service
public class NotificacionServiceImpl implements NotificacionService {

    private static final ZoneId ZONA_HORARIA_OPERATIVA = ZoneId.of("America/Guayaquil");

    private final NotificacionBeneficiarioRepository notificacionBeneficiarioRepository;
    private final LineaPagoService lineaPagoService;
    private final LotePagoService lotePagoService;
    private final AuditoriaSwitchService auditoriaSwitchService;
    private final ObjectMapper objectMapper;
    private final EntityManager entityManager;
    private final NotificacionBeneficiarioMapper notificacionBeneficiarioMapper;

    public NotificacionServiceImpl(NotificacionBeneficiarioRepository notificacionBeneficiarioRepository,
                                   LineaPagoService lineaPagoService,
                                   LotePagoService lotePagoService,
                                   AuditoriaSwitchService auditoriaSwitchService,
                                   ObjectMapper objectMapper,
                                   EntityManager entityManager,
                                   NotificacionBeneficiarioMapper notificacionBeneficiarioMapper) {
        this.notificacionBeneficiarioRepository = notificacionBeneficiarioRepository;
        this.lineaPagoService = lineaPagoService;
        this.lotePagoService = lotePagoService;
        this.auditoriaSwitchService = auditoriaSwitchService;
        this.objectMapper = objectMapper;
        this.entityManager = entityManager;
        this.notificacionBeneficiarioMapper = notificacionBeneficiarioMapper;
    }

    @Override
    @Transactional
    public void registrarNotificacionesBeneficiarios(UUID uuidLote) {
        String rucEmpresa = lotePagoService.obtenerDatosProcesamiento(uuidLote).rucEmpresa();
        lineaPagoService.listarLineasPorLoteUuidYEstado(uuidLote, EstadoLineaPago.EXITOSA)
                .forEach(linea -> registrarNotificacionLineaExitosa(linea, rucEmpresa));
        enviarNotificacionesPendientes();
    }

    @Override
    @Transactional
    public void registrarNotificacionLineaExitosa(LineaPagoInternalDto lineaPagoInternalDto, String rucEmpresa) {
        if (lineaPagoInternalDto.correoNotificacion() == null || lineaPagoInternalDto.correoNotificacion().isBlank()) {
            return;
        }
        if (Boolean.TRUE.equals(notificacionBeneficiarioRepository.existsByLineaPagoIdLinea(lineaPagoInternalDto.idLinea()))) {
            return;
        }
        NotificacionBeneficiario notificacion = notificacionBeneficiarioMapper.toEntity(
                entityManager.getReference(LineaPago.class, lineaPagoInternalDto.idLinea()),
                lineaPagoInternalDto,
                construirContenidoNotificacion(lineaPagoInternalDto, rucEmpresa),
                esCorreoValido(lineaPagoInternalDto.correoNotificacion()),
                OffsetDateTime.now(ZONA_HORARIA_OPERATIVA)
        );
        notificacionBeneficiarioRepository.save(notificacion);
        registrarAuditoria("CREACION_NOTIFICACION", rucEmpresa, notificacion.getCorreoDestino(), notificacion.getEstadoEnvio().name());
    }

    @Override
    @Transactional
    public void enviarNotificacionesPendientes() {
        notificacionBeneficiarioRepository.findByEstadoEnvio(EstadoEnvioNotificacion.PENDIENTE)
                .forEach(this::simularEnvio);
    }

    private void simularEnvio(NotificacionBeneficiario notificacion) {
        if (esCorreoValido(notificacion.getCorreoDestino())) {
            notificacion.setEstadoEnvio(EstadoEnvioNotificacion.ENVIADA);
            notificacion.setFechaEnvio(OffsetDateTime.now(ZONA_HORARIA_OPERATIVA));
            notificacion.setErrorEnvio(null);
        } else {
            notificacion.setEstadoEnvio(EstadoEnvioNotificacion.ERROR);
            notificacion.setErrorEnvio("Correo de notificacion invalido.");
        }
        notificacion.setFechaActualizacion(OffsetDateTime.now(ZONA_HORARIA_OPERATIVA));
        notificacionBeneficiarioRepository.save(notificacion);
        registrarAuditoria("ENVIO_NOTIFICACION_SIMULADO", null, notificacion.getCorreoDestino(),
                notificacion.getEstadoEnvio().name());
    }

    private ObjectNode construirContenidoNotificacion(LineaPagoInternalDto linea, String rucEmpresa) {
        ObjectNode contenido = objectMapper.createObjectNode();
        contenido.put("montoAcreditado", linea.monto());
        contenido.put("concepto", linea.conceptoReferencia());
        contenido.put("empresaEmisora", rucEmpresa);
        contenido.put("cuentaDestino", enmascararCuenta(linea.cuentaDestino()));
        return contenido;
    }

    private String enmascararCuenta(String cuentaDestino) {
        if (cuentaDestino == null || cuentaDestino.length() <= 4) {
            return "****";
        }
        return "****" + cuentaDestino.substring(cuentaDestino.length() - 4);
    }

    private Boolean esCorreoValido(String correo) {
        return correo != null && correo.contains("@") && correo.contains(".");
    }

    private void registrarAuditoria(String accion, String rucEmpresa, String correoDestino, String estado) {
        RegistroAuditoriaRequest request = new RegistroAuditoriaRequest();
        request.setTipoActor(TipoActorAuditoria.SISTEMA);
        request.setIdActor("SWITCH");
        request.setRucEmpresa(rucEmpresa);
        request.setAccion(accion);
        request.setEntidad("NOTIFICACION_BENEFICIARIO");
        request.setIdEntidad(correoDestino);
        ObjectNode datos = objectMapper.createObjectNode();
        datos.put("correoDestino", correoDestino);
        datos.put("estado", estado);
        request.setDatosDespues(datos);
        auditoriaSwitchService.registrarAccion(request);
    }
}
