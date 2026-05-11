package com.banquito.switchpagos.lote.repository;

import com.banquito.switchpagos.catalogo.model.TipoServicio;
import com.banquito.switchpagos.lote.enums.EstadoLote;
import com.banquito.switchpagos.lote.model.LotePago;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

public interface LotePagoRepository extends JpaRepository<LotePago, Long> {

    Optional<LotePago> findByUuidLote(UUID uuidLote);

    Boolean existsByRucEmpresaAndNombreArchivoAndHashArchivoAndFechaRecepcionAfter(
            String rucEmpresa,
            String nombreArchivo,
            String hashArchivo,
            OffsetDateTime fechaLimite
    );

    Boolean existsByRucEmpresaAndNombreArchivoAndHashArchivoAndFechaRecepcionAfterAndIdLoteNot(
            String rucEmpresa,
            String nombreArchivo,
            String hashArchivo,
            OffsetDateTime fechaLimite,
            Long idLote
    );

    @Query("""
            select lotePago
            from LotePago lotePago
            where (:rucEmpresa is null or lotePago.rucEmpresa = :rucEmpresa)
              and (:estado is null or lotePago.estado = :estado)
              and (:tipoServicio is null or lotePago.tipoServicio = :tipoServicio)
              and (:fechaDesde is null or lotePago.fechaRecepcion >= :fechaDesde)
              and (:fechaHasta is null or lotePago.fechaRecepcion <= :fechaHasta)
            """)
    Page<LotePago> consultarPorFiltros(@Param("rucEmpresa") String rucEmpresa,
                                       @Param("estado") EstadoLote estado,
                                       @Param("tipoServicio") TipoServicio tipoServicio,
                                       @Param("fechaDesde") OffsetDateTime fechaDesde,
                                       @Param("fechaHasta") OffsetDateTime fechaHasta,
                                       Pageable pageable);
}
