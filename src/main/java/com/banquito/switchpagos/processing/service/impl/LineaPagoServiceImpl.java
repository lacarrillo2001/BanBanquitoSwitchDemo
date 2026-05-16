package com.banquito.switchpagos.processing.service.impl;

import com.banquito.switchpagos.file.dto.internal.DetalleArchivoPagoInternalDto;
import com.banquito.switchpagos.batch.model.LotePago;
import com.banquito.switchpagos.processing.dto.internal.LineaPagoInternalDto;
import com.banquito.switchpagos.processing.enums.EstadoLineaPago;
import com.banquito.switchpagos.processing.mapper.LineaPagoMapper;
import com.banquito.switchpagos.processing.model.LineaPago;
import com.banquito.switchpagos.processing.repository.LineaPagoRepository;
import com.banquito.switchpagos.processing.service.LineaPagoService;
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
    private final LineaPagoMapper lineaPagoMapper;

    public LineaPagoServiceImpl(LineaPagoRepository lineaPagoRepository,
                                LineaPagoMapper lineaPagoMapper) {
        this.lineaPagoRepository = lineaPagoRepository;
        this.lineaPagoMapper = lineaPagoMapper;
    }

    @Override
    @Transactional
    public void guardarLineasPendientes(LotePago lotePago, List<DetalleArchivoPagoInternalDto> detalles) {
        lineaPagoRepository.deleteByLotePago(lotePago);
        List<LineaPago> lineas = detalles.stream()
                .map(detalle -> lineaPagoMapper.toEntity(lotePago, detalle))
                .toList();
        lineaPagoRepository.saveAll(lineas);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<LineaPagoInternalDto> consultarLineas(LotePago lotePago, EstadoLineaPago estado, Pageable pageable) {
        Page<LineaPago> lineas = estado == null
                ? lineaPagoRepository.findByLotePago(lotePago, pageable)
                : lineaPagoRepository.findByLotePagoAndEstado(lotePago, estado, pageable);
        return lineas.map(lineaPagoMapper::toInternalDto);
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
                .map(lineaPagoMapper::toInternalDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<LineaPagoInternalDto> listarLineasPorLoteUuidYEstado(UUID uuidLote, EstadoLineaPago estado) {
        return lineaPagoRepository.findByLotePagoUuidLoteAndEstadoOrderBySecuencialAsc(uuidLote, estado)
                .stream()
                .map(lineaPagoMapper::toInternalDto)
                .toList();
    }
}
