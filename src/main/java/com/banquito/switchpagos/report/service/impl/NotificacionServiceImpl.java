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
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.UUID;

@Service
public class NotificacionServiceImpl implements NotificacionService {

    private static final Logger LOGGER = LoggerFactory.getLogger(NotificacionServiceImpl.class);
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
    private final String smtpHost;
    private final Integer smtpPort;

    public NotificacionServiceImpl(NotificacionBeneficiarioRepository notificacionBeneficiarioRepository,
                                   LineaPagoService lineaPagoService,
                                   LotePagoService lotePagoService,
                                   AuditoriaSwitchService auditoriaSwitchService,
                                   ObjectMapper objectMapper,
                                   EntityManager entityManager,
                                   NotificacionBeneficiarioMapper notificacionBeneficiarioMapper,
                                   JavaMailSender mailSender,
                                   @Value("${spring.mail.from}") String remitenteNotificaciones,
                                   @Value("${spring.mail.host:}") String smtpHost,
                                   @Value("${spring.mail.port:0}") Integer smtpPort) {
        this.notificacionBeneficiarioRepository = notificacionBeneficiarioRepository;
        this.lineaPagoService = lineaPagoService;
        this.lotePagoService = lotePagoService;
        this.auditoriaSwitchService = auditoriaSwitchService;
        this.objectMapper = objectMapper;
        this.entityManager = entityManager;
        this.notificacionBeneficiarioMapper = notificacionBeneficiarioMapper;
        this.mailSender = mailSender;
        this.remitenteNotificaciones = remitenteNotificaciones;
        this.smtpHost = smtpHost;
        this.smtpPort = smtpPort;
        LOGGER.info("SMTP configurado para notificaciones host={} port={} from={}",
                this.smtpHost, this.smtpPort, this.remitenteNotificaciones);
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

    @Override
    @Transactional
    public void enviarEmailPruebaDirecto(String destinatario, String asunto, String cuerpo) {
        LineaPago ultimaLinea = entityManager.createQuery(
                        "SELECT lineaPago FROM LineaPago lineaPago ORDER BY lineaPago.idLinea DESC",
                        LineaPago.class
                )
                .setMaxResults(1)
                .getResultStream()
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "No existe ninguna linea de pago registrada para asociar la notificacion de prueba."
                ));

        NotificacionBeneficiario notificacion = new NotificacionBeneficiario();
        notificacion.setLineaPago(ultimaLinea);
        notificacion.setCorreoDestino(destinatario);
        notificacion.setTipoNotificacion(TipoNotificacion.PAGO_EXITOSO);
        notificacion.setAsunto(asunto);
        notificacion.setContenido(construirContenidoPrueba(cuerpo, ultimaLinea));
        notificacion.setEstadoEnvio(EstadoEnvioNotificacion.PENDIENTE);
        notificacion.setReintentos(0);
        notificacion.setFechaActualizacion(OffsetDateTime.now(ZONA_HORARIA_OPERATIVA));
        notificacionBeneficiarioRepository.saveAndFlush(notificacion);
        enviarEmailReal(notificacion);
        if (EstadoEnvioNotificacion.ERROR.equals(notificacion.getEstadoEnvio())) {
            throw new IllegalStateException("No fue posible enviar el correo de prueba. " + notificacion.getErrorEnvio());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public String obtenerResumenDb() {
        ObjectNode resumen = objectMapper.createObjectNode();
        resumen.put("totalLotes", Optional.ofNullable(entityManager.createQuery(
                        "SELECT COUNT(lotePago) FROM LotePago lotePago",
                        Long.class
                ).getSingleResult())
                .orElse(0L));
        resumen.put("totalLineas", Optional.ofNullable(entityManager.createQuery(
                        "SELECT COUNT(lineaPago) FROM LineaPago lineaPago",
                        Long.class
                ).getSingleResult())
                .orElse(0L));
        resumen.put("totalNotificaciones", notificacionBeneficiarioRepository.count());
        resumen.put("notificacionesPendientes",
                notificacionBeneficiarioRepository.findByEstadoEnvio(EstadoEnvioNotificacion.PENDIENTE).size());
        resumen.put("notificacionesEnviadas",
                notificacionBeneficiarioRepository.findByEstadoEnvio(EstadoEnvioNotificacion.ENVIADA).size());
        resumen.put("notificacionesError",
                notificacionBeneficiarioRepository.findByEstadoEnvio(EstadoEnvioNotificacion.ERROR).size());
        return resumen.toString();
    }

    private void enviarEmailReal(NotificacionBeneficiario notificacion) {
        try {
            LOGGER.info("Intentando conexion SMTP host={} port={} from={} to={} subject={}",
                    smtpHost,
                    smtpPort,
                    remitenteNotificaciones,
                    notificacion.getCorreoDestino(),
                    notificacion.getAsunto());
            MimeMessageHelper messageHelper = new MimeMessageHelper(
                    mailSender.createMimeMessage(),
                    true,
                    StandardCharsets.UTF_8.name()
            );
            messageHelper.setTo(notificacion.getCorreoDestino());
            messageHelper.setSubject(notificacion.getAsunto());
            messageHelper.setFrom(remitenteNotificaciones);
            messageHelper.setText(construirCuerpoTextoPlano(notificacion), construirCuerpoHtml(notificacion));
            mailSender.send(messageHelper.getMimeMessage());
            LOGGER.info("Conexion SMTP exitosa y correo enviado a {} mediante {}:{}",
                    notificacion.getCorreoDestino(), smtpHost, smtpPort);
            notificacion.setEstadoEnvio(EstadoEnvioNotificacion.ENVIADA);
            notificacion.setFechaEnvio(OffsetDateTime.now(ZONA_HORARIA_OPERATIVA));
            notificacion.setErrorEnvio(null);
        } catch (Exception exception) {
            LOGGER.error("Fallo envio SMTP a {} mediante {}:{}. Motivo: {}",
                    notificacion.getCorreoDestino(), smtpHost, smtpPort, exception.getMessage(), exception);
            notificacion.setEstadoEnvio(EstadoEnvioNotificacion.ERROR);
            notificacion.setErrorEnvio(exception.getMessage());
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
        contenido.put("beneficiario", linea.nombreBeneficiario());
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

    private ObjectNode construirContenidoPrueba(String cuerpo, LineaPago lineaPago) {
        ObjectNode contenido = objectMapper.createObjectNode();
        contenido.put("montoAcreditado", lineaPago.getMonto());
        contenido.put("concepto", cuerpo);
        contenido.put("empresaEmisora", "PRUEBA_SWITCH");
        contenido.put("cuentaDestino", enmascararCuenta(lineaPago.getCuentaDestino()));
        contenido.put("beneficiario", lineaPago.getNombreBeneficiario());
        return contenido;
    }

    private String construirCuerpoTextoPlano(NotificacionBeneficiario notificacion) {
        StringBuilder texto = new StringBuilder();
        texto.append("Banco BanQuito\n\n");
        texto.append("Pago recibido exitosamente\n\n");
        texto.append("Monto acreditado: ").append(formatearMonto(obtenerTextoContenido(notificacion, "montoAcreditado"))).append("\n");
        texto.append("Beneficiario: ").append(obtenerTextoContenido(notificacion, "beneficiario")).append("\n");
        texto.append("Cuenta destino: ").append(obtenerTextoContenido(notificacion, "cuentaDestino")).append("\n");
        texto.append("Concepto: ").append(obtenerTextoContenido(notificacion, "concepto")).append("\n");
        texto.append("Empresa emisora: ").append(obtenerTextoContenido(notificacion, "empresaEmisora")).append("\n");
        texto.append("\nEste correo fue generado automaticamente por Banco BanQuito.");
        return texto.toString();
    }

    private String construirCuerpoHtml(NotificacionBeneficiario notificacion) {
        String monto = formatearMonto(obtenerTextoContenido(notificacion, "montoAcreditado"));
        String beneficiario = escaparHtml(obtenerTextoContenido(notificacion, "beneficiario"));
        String cuentaDestino = escaparHtml(obtenerTextoContenido(notificacion, "cuentaDestino"));
        String concepto = escaparHtml(obtenerTextoContenido(notificacion, "concepto"));
        String empresaEmisora = escaparHtml(obtenerTextoContenido(notificacion, "empresaEmisora"));
        String fechaEnvio = OffsetDateTime.now(ZONA_HORARIA_OPERATIVA)
                .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));

        return """
                <!DOCTYPE html>
                <html lang="es">
                <body style="margin:0;padding:0;background-color:#f3f4f6;font-family:Arial,Helvetica,sans-serif;color:#1f2937;">
                <table role="presentation" width="100%" cellspacing="0" cellpadding="0" style="background-color:#f3f4f6;padding:24px 12px;">
                    <tr>
                        <td align="center">
                            <table role="presentation" width="600" cellspacing="0" cellpadding="0" style="width:600px;max-width:600px;background-color:#ffffff;border-collapse:collapse;border-radius:12px;overflow:hidden;">
                                <tr>
                                    <td style="background-color:#b91c1c;padding:24px 32px;text-align:center;">
                                        <span style="color:#ffffff;font-size:28px;font-weight:700;letter-spacing:0.5px;">Banco BanQuito</span>
                                    </td>
                                </tr>
                                <tr>
                                    <td style="padding:32px;">
                                        <p style="margin:0 0 8px 0;font-size:14px;color:#6b7280;">Notificacion de acreditacion</p>
                                        <h1 style="margin:0 0 24px 0;font-size:28px;line-height:36px;color:#111827;">Pago recibido exitosamente</h1>
                                        <table role="presentation" width="100%" cellspacing="0" cellpadding="0" style="margin-bottom:24px;background-color:#fef2f2;border:1px solid #fecaca;border-radius:10px;">
                                            <tr>
                                                <td style="padding:24px;text-align:center;">
                                                    <p style="margin:0 0 10px 0;font-size:13px;text-transform:uppercase;letter-spacing:1px;color:#991b1b;">Monto acreditado</p>
                                                    <p style="margin:0;font-size:34px;font-weight:700;color:#b91c1c;">__MONTO__</p>
                                                </td>
                                            </tr>
                                        </table>
                                        <table role="presentation" width="100%" cellspacing="0" cellpadding="0" style="border-collapse:collapse;">
                                            <tr>
                                                <td style="padding:12px 0;border-bottom:1px solid #e5e7eb;font-size:14px;color:#6b7280;width:42%;">Beneficiario</td>
                                                <td style="padding:12px 0;border-bottom:1px solid #e5e7eb;font-size:14px;color:#111827;font-weight:600;">__BENEFICIARIO__</td>
                                            </tr>
                                            <tr>
                                                <td style="padding:12px 0;border-bottom:1px solid #e5e7eb;font-size:14px;color:#6b7280;">Cuenta destino</td>
                                                <td style="padding:12px 0;border-bottom:1px solid #e5e7eb;font-size:14px;color:#111827;font-weight:600;">__CUENTA_DESTINO__</td>
                                            </tr>
                                            <tr>
                                                <td style="padding:12px 0;border-bottom:1px solid #e5e7eb;font-size:14px;color:#6b7280;">Concepto</td>
                                                <td style="padding:12px 0;border-bottom:1px solid #e5e7eb;font-size:14px;color:#111827;font-weight:600;">__CONCEPTO__</td>
                                            </tr>
                                            <tr>
                                                <td style="padding:12px 0;border-bottom:1px solid #e5e7eb;font-size:14px;color:#6b7280;">Empresa emisora</td>
                                                <td style="padding:12px 0;border-bottom:1px solid #e5e7eb;font-size:14px;color:#111827;font-weight:600;">__EMPRESA_EMISORA__</td>
                                            </tr>
                                            <tr>
                                                <td style="padding:12px 0 0 0;font-size:14px;color:#6b7280;">Fecha de envio</td>
                                                <td style="padding:12px 0 0 0;font-size:14px;color:#111827;font-weight:600;">__FECHA_ENVIO__</td>
                                            </tr>
                                        </table>
                                    </td>
                                </tr>
                                <tr>
                                    <td style="padding:0 32px 28px 32px;">
                                        <table role="presentation" width="100%" cellspacing="0" cellpadding="0" style="background-color:#f9fafb;border:1px solid #e5e7eb;border-radius:10px;">
                                            <tr>
                                                <td style="padding:18px 20px;font-size:13px;line-height:20px;color:#4b5563;">
                                                    Este correo fue generado automaticamente por Banco BanQuito como constancia informativa del pago recibido.
                                                </td>
                                            </tr>
                                        </table>
                                    </td>
                                </tr>
                            </table>
                        </td>
                    </tr>
                </table>
                </body>
                </html>
                """
                .replace("__MONTO__", monto)
                .replace("__BENEFICIARIO__", beneficiario)
                .replace("__CUENTA_DESTINO__", cuentaDestino)
                .replace("__CONCEPTO__", concepto)
                .replace("__EMPRESA_EMISORA__", empresaEmisora)
                .replace("__FECHA_ENVIO__", fechaEnvio);
    }

    private String obtenerTextoContenido(NotificacionBeneficiario notificacion, String campo) {
        if (notificacion.getContenido() == null || notificacion.getContenido().get(campo) == null) {
            return "No disponible";
        }
        return notificacion.getContenido().get(campo).asText();
    }

    private String formatearMonto(String monto) {
        try {
            return "$" + new BigDecimal(monto).setScale(2, java.math.RoundingMode.HALF_UP);
        } catch (Exception exception) {
            return monto != null ? monto : "No disponible";
        }
    }

    private String escaparHtml(String valor) {
        if (valor == null) {
            return "No disponible";
        }
        return valor
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
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
