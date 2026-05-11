package com.banquito.switchpagos.procesamiento.repository;

import com.banquito.switchpagos.procesamiento.enums.EstadoLimiteTransaccion;
import com.banquito.switchpagos.procesamiento.model.LimiteTransaccion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface LimiteTransaccionRepository extends JpaRepository<LimiteTransaccion, Integer> {

    Optional<LimiteTransaccion> findFirstByTipoServicioCodigoAndEstadoAndVigenteDesdeLessThanEqualAndVigenteHastaIsNullOrderByVigenteDesdeDesc(
            String tipoServicio,
            EstadoLimiteTransaccion estado,
            LocalDate fechaProceso
    );

    Optional<LimiteTransaccion> findFirstByTipoServicioCodigoAndEstadoAndVigenteDesdeLessThanEqualAndVigenteHastaGreaterThanEqualOrderByVigenteDesdeDesc(
            String tipoServicio,
            EstadoLimiteTransaccion estado,
            LocalDate fechaProceso,
            LocalDate fechaProcesoHasta
    );
}
