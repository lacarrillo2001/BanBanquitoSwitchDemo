package com.banquito.switchpagos.file.service;

import com.banquito.switchpagos.file.dto.internal.ArchivoPagoParseadoInternalDto;
import org.springframework.web.multipart.MultipartFile;

public interface ArchivoPagoService {

    ArchivoPagoParseadoInternalDto parsearArchivo(MultipartFile archivo);
}
