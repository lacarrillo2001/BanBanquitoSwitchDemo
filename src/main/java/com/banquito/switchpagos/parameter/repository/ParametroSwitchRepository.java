package com.banquito.switchpagos.parameter.repository;

import com.banquito.switchpagos.parameter.model.ParametroSwitch;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ParametroSwitchRepository extends JpaRepository<ParametroSwitch, String> {

    Optional<ParametroSwitch> findByCodigo(String codigo);

    Boolean existsByCodigo(String codigo);
}
