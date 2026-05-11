package com.banquito.switchpagos.archivo.service;

import com.banquito.switchpagos.archivo.dto.internal.ArchivoPagoParseadoInternalDto;
import org.springframework.web.multipart.MultipartFile;

public interface ArchivoPagoService {

    ArchivoPagoParseadoInternalDto parsearArchivo(MultipartFile archivo);
}
