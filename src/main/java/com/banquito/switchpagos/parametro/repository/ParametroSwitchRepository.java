package com.banquito.switchpagos.parametro.repository;

import com.banquito.switchpagos.parametro.model.ParametroSwitch;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ParametroSwitchRepository extends JpaRepository<ParametroSwitch, String> {

    Optional<ParametroSwitch> findByCodigo(String codigo);

    Boolean existsByCodigo(String codigo);
}
