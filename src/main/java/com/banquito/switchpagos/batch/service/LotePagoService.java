package com.banquito.switchpagos.batch.service;

import com.banquito.switchpagos.batch.dto.api.AnulacionLoteResponse;
import com.banquito.switchpagos.batch.dto.api.CargaLoteResponse;
import com.banquito.switchpagos.batch.dto.api.ConsultaLoteResponse;
import com.banquito.switchpagos.batch.dto.api.EstadoLoteResponse;
import com.banquito.switchpagos.batch.dto.api.LineaPagoResponse;
import com.banquito.switchpagos.batch.dto.api.PaginaResponse;
import com.banquito.switchpagos.batch.dto.api.ValidacionLoteResponse;
import com.banquito.switchpagos.batch.dto.internal.LoteProcesamientoInternalDto;
import com.banquito.switchpagos.batch.dto.internal.RegistroLoteInternalDto;
import com.banquito.switchpagos.batch.enums.EstadoLote;
import com.banquito.switchpagos.processing.enums.EstadoLineaPago;
import org.springframework.data.domain.Pageable;

import java.time.OffsetDateTime;
import java.util.UUID;

public interface LotePagoService {

    CargaLoteResponse registrarLote(RegistroLoteInternalDto registroLoteInternalDto);

    PaginaResponse<ConsultaLoteResponse> consultarLotes(String rucEmpresa, EstadoLote estado, String tipoServicio,
                                                        OffsetDateTime fechaDesde, OffsetDateTime fechaHasta,
                                                        Pageable pageable);

    EstadoLoteResponse consultarEstado(UUID uuidLote);

    AnulacionLoteResponse anularLote(UUID uuidLote, String motivo);

    ValidacionLoteResponse validarLote(UUID uuidLote);

    PaginaResponse<LineaPagoResponse> consultarLineas(UUID uuidLote, EstadoLineaPago estado, Pageable pageable);

    LoteProcesamientoInternalDto obtenerDatosProcesamiento(UUID uuidLote);

    void iniciarProcesamiento(UUID uuidLote, String ejecutadoPor);

    void finalizarProcesamiento(UUID uuidLote, EstadoLote estadoFinal, Integer totalValidadas,
                                Integer totalRechazadas, java.math.BigDecimal montoTotalValidado,
                                String ejecutadoPor);

    void cerrarLoteLiquidado(UUID uuidLote, String ejecutadoPor);
}
