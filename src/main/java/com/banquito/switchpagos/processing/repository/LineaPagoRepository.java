package com.banquito.switchpagos.processing.repository;

import com.banquito.switchpagos.batch.model.LotePago;
import com.banquito.switchpagos.processing.enums.EstadoLineaPago;
import com.banquito.switchpagos.processing.model.LineaPago;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface LineaPagoRepository extends JpaRepository<LineaPago, Long> {

    Page<LineaPago> findByLotePago(LotePago lotePago, Pageable pageable);

    Page<LineaPago> findByLotePagoAndEstado(LotePago lotePago, EstadoLineaPago estado, Pageable pageable);

    List<LineaPago> findByLotePagoUuidLoteAndEstadoInOrderBySecuencialAsc(UUID uuidLote, List<EstadoLineaPago> estados);

    List<LineaPago> findByLotePagoUuidLoteOrderBySecuencialAsc(UUID uuidLote);

    List<LineaPago> findByLotePagoUuidLoteAndEstadoOrderBySecuencialAsc(UUID uuidLote, EstadoLineaPago estado);

    Long countByLotePago(LotePago lotePago);

    Long countByLotePagoAndEstado(LotePago lotePago, EstadoLineaPago estado);

    @Query("select coalesce(sum(lineaPago.monto), 0) from LineaPago lineaPago where lineaPago.lotePago = :lotePago")
    BigDecimal sumarMontoPorLote(@Param("lotePago") LotePago lotePago);

    Long countByLotePagoUuidLote(UUID uuidLote);

    Long countByLotePagoUuidLoteAndEstado(UUID uuidLote, EstadoLineaPago estado);

    @Query("""
            select coalesce(sum(lineaPago.monto), 0)
            from LineaPago lineaPago
            where lineaPago.lotePago.uuidLote = :uuidLote
              and lineaPago.estado = :estado
            """)
    BigDecimal sumarMontoPorLoteUuidYEstado(@Param("uuidLote") UUID uuidLote, @Param("estado") EstadoLineaPago estado);

    void deleteByLotePago(LotePago lotePago);
}
