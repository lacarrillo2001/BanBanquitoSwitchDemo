package com.banquito.switchpagos.tarifaje.repository;

import com.banquito.switchpagos.tarifaje.model.DetalleLiquidacion;
import com.banquito.switchpagos.tarifaje.model.LiquidacionServicio;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DetalleLiquidacionRepository extends JpaRepository<DetalleLiquidacion, Long> {

    List<DetalleLiquidacion> findByLiquidacionServicio(LiquidacionServicio liquidacionServicio);
}
