package com.banquito.switchpagos.batch.repository;

import com.banquito.switchpagos.batch.model.LotePago;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

public interface LotePagoRepository extends JpaRepository<LotePago, Long>, JpaSpecificationExecutor<LotePago> {

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

}
