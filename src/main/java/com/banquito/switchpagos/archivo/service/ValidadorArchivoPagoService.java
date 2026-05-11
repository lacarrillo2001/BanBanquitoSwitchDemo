package com.banquito.switchpagos.archivo.service;

import com.banquito.switchpagos.archivo.dto.internal.ArchivoPagoParseadoInternalDto;
import com.banquito.switchpagos.archivo.dto.internal.ResultadoValidacionArchivoInternalDto;

public interface ValidadorArchivoPagoService {

    ResultadoValidacionArchivoInternalDto validarEstructura(ArchivoPagoParseadoInternalDto archivoPagoParseado);
}
