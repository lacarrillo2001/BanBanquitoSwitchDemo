package com.banquito.switchpagos.report.repository;

import com.banquito.switchpagos.processing.model.LineaPago;
import com.banquito.switchpagos.report.enums.EstadoEnvioNotificacion;
import com.banquito.switchpagos.report.model.NotificacionBeneficiario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificacionBeneficiarioRepository extends JpaRepository<NotificacionBeneficiario, Long> {

    Boolean existsByLineaPagoIdLinea(Long idLinea);

    List<NotificacionBeneficiario> findByLineaPago(LineaPago lineaPago);

    List<NotificacionBeneficiario> findByEstadoEnvio(EstadoEnvioNotificacion estadoEnvio);
}
