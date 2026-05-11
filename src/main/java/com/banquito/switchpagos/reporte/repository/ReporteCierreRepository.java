package com.banquito.switchpagos.reporte.repository;

import com.banquito.switchpagos.reporte.enums.TipoReporte;
import com.banquito.switchpagos.reporte.model.ReporteCierre;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ReporteCierreRepository extends JpaRepository<ReporteCierre, Long> {

    Optional<ReporteCierre> findByLotePagoUuidLoteAndTipoReporte(UUID uuidLote, TipoReporte tipoReporte);

    Boolean existsByLotePagoUuidLoteAndTipoReporte(UUID uuidLote, TipoReporte tipoReporte);
}
