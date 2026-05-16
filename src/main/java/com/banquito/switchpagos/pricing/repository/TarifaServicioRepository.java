package com.banquito.switchpagos.pricing.repository;

import com.banquito.switchpagos.pricing.enums.EstadoTarifaServicio;
import com.banquito.switchpagos.pricing.model.TarifaServicio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TarifaServicioRepository extends JpaRepository<TarifaServicio, Integer> {

    @Query("""
            select tarifaServicio
            from TarifaServicio tarifaServicio
            where tarifaServicio.estado = :estado
              and tarifaServicio.vigenteDesde <= :fechaActual
              and (tarifaServicio.vigenteHasta is null or tarifaServicio.vigenteHasta >= :fechaActual)
              and (:tipoServicio is null or tarifaServicio.tipoServicio.codigo = :tipoServicio)
            order by tarifaServicio.tipoServicio.codigo, tarifaServicio.rangoDesde
            """)
    List<TarifaServicio> consultarTarifasVigentes(@Param("tipoServicio") String tipoServicio,
                                                  @Param("estado") EstadoTarifaServicio estado,
                                                  @Param("fechaActual") LocalDate fechaActual);

    @Query("""
            select tarifaServicio
            from TarifaServicio tarifaServicio
            where tarifaServicio.tipoServicio.codigo = :tipoServicio
              and tarifaServicio.estado = :estado
              and tarifaServicio.vigenteDesde <= :fechaActual
              and (tarifaServicio.vigenteHasta is null or tarifaServicio.vigenteHasta >= :fechaActual)
              and tarifaServicio.rangoDesde <= :transaccionesExitosas
              and (tarifaServicio.rangoHasta is null or tarifaServicio.rangoHasta >= :transaccionesExitosas)
            order by tarifaServicio.rangoDesde desc
            """)
    Optional<TarifaServicio> buscarTarifaAplicable(@Param("tipoServicio") String tipoServicio,
                                                  @Param("transaccionesExitosas") Integer transaccionesExitosas,
                                                  @Param("estado") EstadoTarifaServicio estado,
                                                  @Param("fechaActual") LocalDate fechaActual);
}
