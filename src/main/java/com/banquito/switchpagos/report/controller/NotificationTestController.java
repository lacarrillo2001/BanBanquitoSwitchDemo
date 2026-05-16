package com.banquito.switchpagos.report.controller;

import com.banquito.switchpagos.report.service.MailpitService;
import com.banquito.switchpagos.report.service.NotificacionService;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/report/notifications")
public class NotificationTestController {

    private final NotificacionService notificacionService;
    private final MailpitService mailpitService;

    public NotificationTestController(NotificacionService notificacionService, MailpitService mailpitService) {
        this.notificacionService = notificacionService;
        this.mailpitService = mailpitService;
    }

    @PostMapping("/send-pending")
    public ResponseEntity<String> enviarPendientes() {
        notificacionService.enviarNotificacionesPendientes();
        return ResponseEntity.ok("Proceso de envio de notificaciones pendientes iniciado.");
    }

    @PostMapping("/test-email")
    public ResponseEntity<String> enviarEmailPrueba(@RequestParam String email) {
        // Para pruebas, creamos una notificación manual en la tabla existente
        notificacionService.enviarEmailPruebaDirecto(email, "Prueba BanQuito", "Correo de prueba usando tabla NOTIFICACION_BENEFICIARIO");
        return ResponseEntity.ok("Correo de prueba enviado.");
    }

    @GetMapping("/mailpit/messages")
    public ResponseEntity<JsonNode> listarMensajesMailpit() {
        return ResponseEntity.ok(mailpitService.listarMensajes());
    }

    @GetMapping("/mailpit/messages/{id}")
    public ResponseEntity<JsonNode> obtenerMensajeMailpit(@PathVariable String id) {
        return ResponseEntity.ok(mailpitService.obtenerMensaje(id));
    }

    @DeleteMapping("/mailpit/messages")
    public ResponseEntity<Void> borrarMensajesMailpit() {
        mailpitService.borrarTodosLosMensajes();
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/db-status")
    public ResponseEntity<String> obtenerEstadoDb() {
        return ResponseEntity.ok(notificacionService.obtenerResumenDb());
    }
}
