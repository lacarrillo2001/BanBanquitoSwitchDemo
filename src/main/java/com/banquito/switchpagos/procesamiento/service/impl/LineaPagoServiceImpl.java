package com.banquito.switchpagos.procesamiento.service.impl;

import com.banquito.switchpagos.archivo.dto.internal.DetalleArchivoPagoInternalDto;
import com.banquito.switchpagos.lote.model.LotePago;
import com.banquito.switchpagos.procesamiento.dto.internal.LineaPagoInternalDto;
import com.banquito.switchpagos.procesamiento.enums.EstadoLineaPago;
import com.banquito.switchpagos.procesamiento.model.LineaPago;
import com.banquito.switchpagos.procesamiento.repository.LineaPagoRepository;
import com.banquito.switchpagos.procesamiento.service.LineaPagoService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
public class LineaPagoServiceImpl implements LineaPagoService {

    private final LineaPagoRepository lineaPagoRepository;

    public LineaPagoServiceImpl(LineaPagoRepository lineaPagoRepository) {
        this.lineaPagoRepository = lineaPagoRepository;
    }

    @Override
    @Transactional
    public void guardarLineasPendientes(LotePago lotePago, List<DetalleArchivoPagoInternalDto> detalles) {
        lineaPagoRepository.deleteByLotePago(lotePago);
        List<LineaPago> lineas = detalles.stream()
                .map(detalle -> construirLineaPago(lotePago, detalle))
                .toList();
        lineaPagoRepository.saveAll(lineas);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<LineaPagoInternalDto> consultarLineas(LotePago lotePago, EstadoLineaPago estado, Pageable pageable) {
        Page<LineaPago> lineas = estado == null
                ? lineaPagoRepository.findByLotePago(lotePago, pageable)
                : lineaPagoRepository.findByLotePagoAndEstado(lotePago, estado, pageable);
        return lineas.map(this::construirInternalDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Long contarLineas(LotePago lotePago) {
        return lineaPagoRepository.countByLotePago(lotePago);
    }

    @Override
    @Transactional(readOnly = true)
    public Long contarLineasPorEstado(LotePago lotePago, EstadoLineaPago estado) {
        return lineaPagoRepository.countByLotePagoAndEstado(lotePago, estado);
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal sumarMontoLineas(LotePago lotePago) {
        return lineaPagoRepository.sumarMontoPorLote(lotePago);
    }

    @Override
    @Transactional(readOnly = true)
    public Long contarLineasPorLoteUuidYEstado(UUID uuidLote, EstadoLineaPago estado) {
        return lineaPagoRepository.countByLotePagoUuidLoteAndEstado(uuidLote, estado);
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal sumarMontoPorLoteUuidYEstado(UUID uuidLote, EstadoLineaPago estado) {
        return lineaPagoRepository.sumarMontoPorLoteUuidYEstado(uuidLote, estado);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LineaPagoInternalDto> listarLineasPorLoteUuid(UUID uuidLote) {
        return lineaPagoRepository.findByLotePagoUuidLoteOrderBySecuencialAsc(uuidLote)
                .stream()
                .map(this::construirInternalDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<LineaPagoInternalDto> listarLineasPorLoteUuidYEstado(UUID uuidLote, EstadoLineaPago estado) {
        return lineaPagoRepository.findByLotePagoUuidLoteAndEstadoOrderBySecuencialAsc(uuidLote, estado)
                .stream()
                .map(this::construirInternalDto)
                .toList();
    }

    private LineaPago construirLineaPago(LotePago lotePago, DetalleArchivoPagoInternalDto detalle) {
        LineaPago lineaPago = new LineaPago();
        lineaPago.setLotePago(lotePago);
        lineaPago.setSecuencial(detalle.secuencial());
        lineaPago.setIdentificacionBeneficiario(detalle.identificacionBeneficiario());
        lineaPago.setNombreBeneficiario(detalle.nombreBeneficiario());
        lineaPago.setCuentaDestino(detalle.cuentaDestino());
        lineaPago.setMonto(detalle.monto());
        lineaPago.setConceptoReferencia(detalle.conceptoReferencia());
        lineaPago.setCorreoNotificacion(detalle.correoNotificacion());
        lineaPago.setEstado(EstadoLineaPago.PENDIENTE);
        lineaPago.setUuidOperacionSwitch(UUID.randomUUID());
        return lineaPago;
    }

    private LineaPagoInternalDto construirInternalDto(LineaPago lineaPago) {
        return new LineaPagoInternalDto(
                lineaPago.getIdLinea(),
                lineaPago.getUuidOperacionSwitch(),
                lineaPago.getSecuencial(),
                lineaPago.getIdentificacionBeneficiario(),
                lineaPago.getNombreBeneficiario(),
                lineaPago.getCuentaDestino(),
                lineaPago.getMonto(),
                lineaPago.getConceptoReferencia(),
                lineaPago.getCorreoNotificacion(),
                lineaPago.getEstado() != null ? lineaPago.getEstado().name() : null,
                lineaPago.getCodigoError(),
                lineaPago.getMensajeError(),
                lineaPago.getFechaValidacion()
        );
    }
}
