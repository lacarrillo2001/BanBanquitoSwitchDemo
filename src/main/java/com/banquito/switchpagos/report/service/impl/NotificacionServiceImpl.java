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
import com.banquito.switchpagos.report.enums.TipoNotificacion;
import com.banquito.switchpagos.report.mapper.NotificacionBeneficiarioMapper;
import com.banquito.switchpagos.report.model.NotificacionBeneficiario;
import com.banquito.switchpagos.report.repository.NotificacionBeneficiarioRepository;
import com.banquito.switchpagos.report.service.NotificacionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;
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
    private final JavaMailSender mailSender;
    private final String remitenteNotificaciones;

    public NotificacionServiceImpl(NotificacionBeneficiarioRepository notificacionBeneficiarioRepository,
                                   LineaPagoService lineaPagoService,
                                   LotePagoService lotePagoService,
                                   AuditoriaSwitchService auditoriaSwitchService,
                                   ObjectMapper objectMapper,
                                   EntityManager entityManager,
                                   NotificacionBeneficiarioMapper notificacionBeneficiarioMapper,
                                   JavaMailSender mailSender,
                                   @Value("${spring.mail.from}") String remitenteNotificaciones) {
        this.notificacionBeneficiarioRepository = notificacionBeneficiarioRepository;
        this.lineaPagoService = lineaPagoService;
        this.lotePagoService = lotePagoService;
        this.auditoriaSwitchService = auditoriaSwitchService;
        this.objectMapper = objectMapper;
        this.entityManager = entityManager;
        this.notificacionBeneficiarioMapper = notificacionBeneficiarioMapper;
        this.mailSender = mailSender;
        this.remitenteNotificaciones = remitenteNotificaciones;
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
                .forEach(this::enviarEmailReal);
    }

    private void enviarEmailReal(NotificacionBeneficiario notificacion) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(notificacion.getCorreoDestino());
            message.setSubject(notificacion.getAsunto());
            message.setText("Notificacion de Pago BanQuito: " + notificacion.getContenido().toString());
            message.setFrom(remitenteNotificaciones);

            mailSender.send(message);

            notificacion.setEstadoEnvio(EstadoEnvioNotificacion.ENVIADA);
            notificacion.setFechaEnvio(OffsetDateTime.now(ZONA_HORARIA_OPERATIVA));
            notificacion.setErrorEnvio(null);
        } catch (Exception e) {
            notificacion.setEstadoEnvio(EstadoEnvioNotificacion.ERROR);
            notificacion.setErrorEnvio(e.getMessage());
        }
        notificacion.setFechaActualizacion(OffsetDateTime.now(ZONA_HORARIA_OPERATIVA));
        notificacionBeneficiarioRepository.saveAndFlush(notificacion);
        registrarAuditoria("ENVIO_NOTIFICACION_REAL", null, notificacion.getCorreoDestino(),
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

    @Override
    @Transactional
    public void enviarEmailPruebaDirecto(String destinatario, String asunto, String cuerpo) {
        // Buscamos la última línea de pago para cumplir con la integridad referencial (ID_LINEA NOT NULL)
        List<LineaPago> lineas = entityManager.createQuery("SELECT l FROM LineaPago l ORDER BY l.idLinea DESC", LineaPago.class)
                .setMaxResults(1)
                .getResultList();

        if (lineas.isEmpty()) {
            throw new RuntimeException("No existen líneas de pago en la base de datos para realizar la prueba. Por favor, procesa un lote primero.");
        }

        NotificacionBeneficiario notificacion = new NotificacionBeneficiario();
        notificacion.setLineaPago(lineas.get(0)); // Asociamos a una línea existente
        notificacion.setCorreoDestino(destinatario);
        notificacion.setAsunto(asunto);
        notificacion.setTipoNotificacion(TipoNotificacion.PAGO_EXITOSO);
        notificacion.setEstadoEnvio(EstadoEnvioNotificacion.PENDIENTE);
        
        ObjectNode contenido = objectMapper.createObjectNode();
        contenido.put("mensaje", cuerpo);
        notificacion.setContenido(contenido);
        
        notificacion.setReintentos(0);
        notificacion.setFechaActualizacion(OffsetDateTime.now(ZONA_HORARIA_OPERATIVA));
        
        notificacionBeneficiarioRepository.saveAndFlush(notificacion);
        enviarEmailReal(notificacion);
    }

    @Override
    @Transactional(readOnly = true)
    public String obtenerResumenDb() {
        Long totalLotes = (Long) entityManager.createQuery("SELECT COUNT(l) FROM LotePago l").getSingleResult();
        Long totalLineas = (Long) entityManager.createQuery("SELECT COUNT(l) FROM LineaPago l").getSingleResult();
        Long totalNotificaciones = (Long) entityManager.createQuery("SELECT COUNT(n) FROM NotificacionBeneficiario n").getSingleResult();

        return String.format("Resumen de Base de Datos:\n- Lotes de Pago: %d\n- Líneas de Pago: %d\n- Notificaciones Registradas: %d", 
                totalLotes, totalLineas, totalNotificaciones);
    }
}
