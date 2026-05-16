package com.banquito.switchpagos.batch.mapper;

import com.banquito.switchpagos.file.dto.internal.ArchivoPagoParseadoInternalDto;
import com.banquito.switchpagos.catalog.model.TipoServicio;
import com.banquito.switchpagos.batch.dto.api.AnulacionLoteResponse;
import com.banquito.switchpagos.batch.dto.api.CargaLoteResponse;
import com.banquito.switchpagos.batch.dto.api.ConsultaLoteResponse;
import com.banquito.switchpagos.batch.dto.api.EstadoLoteResponse;
import com.banquito.switchpagos.batch.dto.api.FechasLoteResponse;
import com.banquito.switchpagos.batch.dto.api.ResumenEstadoLoteResponse;
import com.banquito.switchpagos.batch.dto.api.TotalesValidacionResponse;
import com.banquito.switchpagos.batch.dto.api.ValidacionLoteResponse;
import com.banquito.switchpagos.batch.dto.internal.LoteProcesamientoInternalDto;
import com.banquito.switchpagos.batch.dto.internal.RegistroLoteInternalDto;
import com.banquito.switchpagos.batch.enums.EstadoLote;
import com.banquito.switchpagos.batch.enums.FormatoArchivo;
import com.banquito.switchpagos.batch.model.LotePago;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Component
public class LotePagoMapper {

    public LotePago toEntity(RegistroLoteInternalDto registroLoteInternalDto,
                             ArchivoPagoParseadoInternalDto archivoPagoParseado,
                             TipoServicio tipoServicio,
                             FormatoArchivo formatoArchivo,
                             EstadoLote estadoInicial,
                             OffsetDateTime fechaRecepcion) {
        LotePago lotePago = new LotePago();
        lotePago.setUuidLote(UUID.randomUUID());
        lotePago.setClaveIdempotencia(UUID.randomUUID());
        lotePago.setRucEmpresa(archivoPagoParseado.cabecera().rucEmpresa());
        lotePago.setIdCredencialWebCore(registroLoteInternalDto.idCredencialWebCore());
        lotePago.setTipoServicio(tipoServicio);
        lotePago.setCuentaMatrizCargo(archivoPagoParseado.cabecera().cuentaMatrizCargo());
        lotePago.setFechaHoraGeneracion(archivoPagoParseado.cabecera().fechaHoraGeneracion());
        lotePago.setTotalRegistrosDeclarado(archivoPagoParseado.cabecera().totalRegistrosDeclarado());
        lotePago.setMontoTotalDeclarado(archivoPagoParseado.cabecera().montoTotalDeclarado());
        lotePago.setTotalRegistrosPie(archivoPagoParseado.pie().totalRegistrosPie());
        lotePago.setMontoTotalPie(archivoPagoParseado.pie().montoTotalPie());
        lotePago.setNombreArchivo(archivoPagoParseado.nombreArchivo());
        lotePago.setHashArchivo(archivoPagoParseado.hashArchivo());
        lotePago.setHashPieControl(archivoPagoParseado.pie().hashPieControl());
        lotePago.setTamanoBytes(archivoPagoParseado.tamanoBytes());
        lotePago.setFormatoArchivo(formatoArchivo);
        lotePago.setCanalIngreso(registroLoteInternalDto.canalIngreso());
        lotePago.setEstado(estadoInicial);
        lotePago.setFechaRecepcion(fechaRecepcion);
        return lotePago;
    }

    public CargaLoteResponse toCargaResponse(LotePago lotePago) {
        return new CargaLoteResponse(
                lotePago.getUuidLote(),
                lotePago.getEstado().name(),
                lotePago.getNombreArchivo(),
                lotePago.getHashArchivo(),
                "Lote registrado correctamente.",
                "VALIDAR"
        );
    }

    public ConsultaLoteResponse toConsultaResponse(LotePago lotePago) {
        return new ConsultaLoteResponse(
                lotePago.getUuidLote(),
                lotePago.getRucEmpresa(),
                lotePago.getTipoServicio() != null ? lotePago.getTipoServicio().getCodigo() : null,
                lotePago.getNombreArchivo(),
                lotePago.getCanalIngreso() != null ? lotePago.getCanalIngreso().name() : null,
                lotePago.getEstado() != null ? lotePago.getEstado().name() : null,
                lotePago.getTotalRegistrosDeclarado(),
                lotePago.getMontoTotalDeclarado(),
                lotePago.getFechaRecepcion()
        );
    }

    public EstadoLoteResponse toEstadoResponse(LotePago lotePago,
                                               ResumenEstadoLoteResponse resumen,
                                               List<String> accionesDisponibles) {
        return new EstadoLoteResponse(
                lotePago.getUuidLote(),
                lotePago.getEstado().name(),
                lotePago.getMotivoRechazoGlobal(),
                resumen,
                new FechasLoteResponse(
                        lotePago.getFechaRecepcion(),
                        lotePago.getFechaInicioValidacion(),
                        lotePago.getFechaFinValidacion(),
                        lotePago.getFechaInicioProceso(),
                        lotePago.getFechaFinProceso(),
                        lotePago.getFechaCierre()
                ),
                accionesDisponibles
        );
    }

    public AnulacionLoteResponse toAnulacionResponse(LotePago lotePago, String motivo) {
        return new AnulacionLoteResponse(lotePago.getUuidLote(), lotePago.getEstado().name(), motivo);
    }

    public ValidacionLoteResponse toValidacionResponse(LotePago lotePago, Boolean valido,
                                                       TotalesValidacionResponse totales,
                                                       List<com.banquito.switchpagos.batch.dto.api.ErrorGlobalResponse> errores) {
        return new ValidacionLoteResponse(lotePago.getUuidLote(), lotePago.getEstado().name(), valido, totales, errores);
    }

    public TotalesValidacionResponse toTotalesValidacionResponse(LotePago lotePago, Long totalLineas,
                                                                 BigDecimal montoTotalDetalle) {
        return new TotalesValidacionResponse(
                lotePago.getTotalRegistrosDeclarado(),
                lotePago.getTotalRegistrosPie(),
                totalLineas,
                lotePago.getMontoTotalDeclarado(),
                lotePago.getMontoTotalPie(),
                montoTotalDetalle
        );
    }

    public LoteProcesamientoInternalDto toProcesamientoInternalDto(LotePago lotePago) {
        return new LoteProcesamientoInternalDto(
                lotePago.getIdLote(),
                lotePago.getUuidLote(),
                lotePago.getRucEmpresa(),
                lotePago.getTipoServicio() != null ? lotePago.getTipoServicio().getCodigo() : null,
                lotePago.getCuentaMatrizCargo(),
                lotePago.getEstado(),
                lotePago.getFechaInicioProceso(),
                lotePago.getFechaFinProceso()
        );
    }

    public ObjectNode toDatosBasicosNode(LotePago lotePago, ObjectMapper objectMapper) {
        ObjectNode datos = objectMapper.createObjectNode();
        datos.put("uuidLote", lotePago.getUuidLote().toString());
        datos.put("estado", lotePago.getEstado().name());
        datos.put("rucEmpresa", lotePago.getRucEmpresa());
        datos.put("nombreArchivo", lotePago.getNombreArchivo());
        datos.put("hashArchivo", lotePago.getHashArchivo());
        return datos;
    }
}
