package com.banquito.switchpagos.processing.service;

import com.banquito.switchpagos.file.dto.internal.DetalleArchivoPagoInternalDto;
import com.banquito.switchpagos.batch.model.LotePago;
import com.banquito.switchpagos.processing.dto.internal.LineaPagoInternalDto;
import com.banquito.switchpagos.processing.enums.EstadoLineaPago;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;

public interface LineaPagoService {

    void guardarLineasPendientes(LotePago lotePago, List<DetalleArchivoPagoInternalDto> detalles);

    Page<LineaPagoInternalDto> consultarLineas(LotePago lotePago, EstadoLineaPago estado, Pageable pageable);

    Long contarLineas(LotePago lotePago);

    Long contarLineasPorEstado(LotePago lotePago, EstadoLineaPago estado);

    BigDecimal sumarMontoLineas(LotePago lotePago);

    Long contarLineasPorLoteUuidYEstado(java.util.UUID uuidLote, EstadoLineaPago estado);

    BigDecimal sumarMontoPorLoteUuidYEstado(java.util.UUID uuidLote, EstadoLineaPago estado);

    java.util.List<LineaPagoInternalDto> listarLineasPorLoteUuid(java.util.UUID uuidLote);

    java.util.List<LineaPagoInternalDto> listarLineasPorLoteUuidYEstado(java.util.UUID uuidLote,
                                                                        EstadoLineaPago estado);
}
