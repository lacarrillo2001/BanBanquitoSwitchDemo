package com.banquito.switchpagos.file.service.impl;

import com.banquito.switchpagos.file.dto.internal.ArchivoPagoParseadoInternalDto;
import com.banquito.switchpagos.file.dto.internal.DetalleArchivoPagoInternalDto;
import com.banquito.switchpagos.file.dto.internal.ErrorValidacionArchivoInternalDto;
import com.banquito.switchpagos.file.dto.internal.ResultadoValidacionArchivoInternalDto;
import com.banquito.switchpagos.file.service.ValidadorArchivoPagoService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class ValidadorArchivoPagoServiceImpl implements ValidadorArchivoPagoService {

    @Override
    public ResultadoValidacionArchivoInternalDto validarEstructura(ArchivoPagoParseadoInternalDto archivoPagoParseado) {
        List<ErrorValidacionArchivoInternalDto> errores = new ArrayList<>();
        if (archivoPagoParseado.cabecera() == null) {
            errores.add(new ErrorValidacionArchivoInternalDto("CABECERA_REQUERIDA", "El archivo debe tener una cabecera."));
        }
        if (archivoPagoParseado.pie() == null) {
            errores.add(new ErrorValidacionArchivoInternalDto("PIE_REQUERIDO", "El archivo debe tener un pie de control."));
        }
        if (archivoPagoParseado.detalles().isEmpty()) {
            errores.add(new ErrorValidacionArchivoInternalDto("DETALLE_REQUERIDO", "El archivo debe tener al menos un detalle."));
        }
        if (!errores.isEmpty()) {
            return new ResultadoValidacionArchivoInternalDto(Boolean.FALSE, errores);
        }

        validarSecuenciales(archivoPagoParseado.detalles(), errores);
        validarTotalesCabecera(archivoPagoParseado, errores);
        validarTotalesPie(archivoPagoParseado, errores);

        return new ResultadoValidacionArchivoInternalDto(errores.isEmpty(), errores);
    }

    private void validarSecuenciales(List<DetalleArchivoPagoInternalDto> detalles,
                                     List<ErrorValidacionArchivoInternalDto> errores) {
        List<DetalleArchivoPagoInternalDto> detallesOrdenados = detalles.stream()
                .sorted(Comparator.comparing(DetalleArchivoPagoInternalDto::secuencial))
                .toList();
        for (Integer indice = 0; indice < detallesOrdenados.size(); indice++) {
            Integer secuencialEsperado = indice + 1;
            Integer secuencialActual = detallesOrdenados.get(indice).secuencial();
            if (!secuencialEsperado.equals(secuencialActual)) {
                errores.add(new ErrorValidacionArchivoInternalDto(
                        "SECUENCIAL_INVALIDO",
                        "Los secuenciales del detalle deben iniciar en 1 y ser consecutivos."
                ));
                return;
            }
        }
    }

    private void validarTotalesCabecera(ArchivoPagoParseadoInternalDto archivoPagoParseado,
                                        List<ErrorValidacionArchivoInternalDto> errores) {
        Integer totalDetalles = archivoPagoParseado.detalles().size();
        BigDecimal montoDetalle = sumarMontoDetalle(archivoPagoParseado.detalles());
        if (!totalDetalles.equals(archivoPagoParseado.cabecera().totalRegistrosDeclarado())) {
            errores.add(new ErrorValidacionArchivoInternalDto(
                    "TOTAL_REGISTROS_DECLARADO_INVALIDO",
                    "El total de registros declarado no coincide con el detalle."
            ));
        }
        if (montoDetalle.compareTo(archivoPagoParseado.cabecera().montoTotalDeclarado()) != 0) {
            errores.add(new ErrorValidacionArchivoInternalDto(
                    "MONTO_DECLARADO_INVALIDO",
                    "El monto total declarado no coincide con la sumatoria del detalle."
            ));
        }
    }

    private void validarTotalesPie(ArchivoPagoParseadoInternalDto archivoPagoParseado,
                                   List<ErrorValidacionArchivoInternalDto> errores) {
        Integer totalDetalles = archivoPagoParseado.detalles().size();
        BigDecimal montoDetalle = sumarMontoDetalle(archivoPagoParseado.detalles());
        if (!totalDetalles.equals(archivoPagoParseado.pie().totalRegistrosPie())) {
            errores.add(new ErrorValidacionArchivoInternalDto(
                    "TOTAL_REGISTROS_PIE_INVALIDO",
                    "El total de registros del pie no coincide con el detalle."
            ));
        }
        if (montoDetalle.compareTo(archivoPagoParseado.pie().montoTotalPie()) != 0) {
            errores.add(new ErrorValidacionArchivoInternalDto(
                    "MONTO_PIE_INVALIDO",
                    "El monto total del pie no coincide con la sumatoria del detalle."
            ));
        }
    }

    private BigDecimal sumarMontoDetalle(List<DetalleArchivoPagoInternalDto> detalles) {
        return detalles.stream()
                .map(DetalleArchivoPagoInternalDto::monto)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
