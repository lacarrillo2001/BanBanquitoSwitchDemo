package com.banquito.switchpagos.pricing.repository;

import com.banquito.switchpagos.pricing.model.LiquidacionServicio;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface LiquidacionServicioRepository extends JpaRepository<LiquidacionServicio, Long> {

    Optional<LiquidacionServicio> findByLotePagoUuidLote(UUID uuidLote);

    Boolean existsByLotePagoUuidLote(UUID uuidLote);
}
