package com.banquito.switchpagos.file.service;

import com.banquito.switchpagos.file.dto.internal.ArchivoPagoParseadoInternalDto;
import com.banquito.switchpagos.file.dto.internal.ResultadoValidacionArchivoInternalDto;

public interface ValidadorArchivoPagoService {

    ResultadoValidacionArchivoInternalDto validarEstructura(ArchivoPagoParseadoInternalDto archivoPagoParseado);
}
