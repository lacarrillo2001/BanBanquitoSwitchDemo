package com.banquito.switchpagos.pricing.repository;

import com.banquito.switchpagos.pricing.model.DetalleLiquidacion;
import com.banquito.switchpagos.pricing.model.LiquidacionServicio;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DetalleLiquidacionRepository extends JpaRepository<DetalleLiquidacion, Long> {

    List<DetalleLiquidacion> findByLiquidacionServicio(LiquidacionServicio liquidacionServicio);
}
