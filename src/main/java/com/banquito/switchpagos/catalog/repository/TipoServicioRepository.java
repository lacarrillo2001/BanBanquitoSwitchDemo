package com.banquito.switchpagos.catalog.repository;

import com.banquito.switchpagos.catalog.enums.EstadoTipoServicio;
import com.banquito.switchpagos.catalog.model.TipoServicio;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TipoServicioRepository extends JpaRepository<TipoServicio, String> {

    Optional<TipoServicio> findByCodigo(String codigo);

    Boolean existsByCodigo(String codigo);

    Boolean existsByCodigoAndEstado(String codigo, EstadoTipoServicio estado);

    List<TipoServicio> findByEstado(EstadoTipoServicio estado);
}
