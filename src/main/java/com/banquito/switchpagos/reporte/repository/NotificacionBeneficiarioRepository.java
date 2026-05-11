package com.banquito.switchpagos.reporte.repository;

import com.banquito.switchpagos.procesamiento.model.LineaPago;
import com.banquito.switchpagos.reporte.enums.EstadoEnvioNotificacion;
import com.banquito.switchpagos.reporte.model.NotificacionBeneficiario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificacionBeneficiarioRepository extends JpaRepository<NotificacionBeneficiario, Long> {

    Boolean existsByLineaPagoIdLinea(Long idLinea);

    List<NotificacionBeneficiario> findByLineaPago(LineaPago lineaPago);

    List<NotificacionBeneficiario> findByEstadoEnvio(EstadoEnvioNotificacion estadoEnvio);
}
