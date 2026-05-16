package com.banquito.switchpagos.file.service;

import com.banquito.switchpagos.file.dto.internal.ArchivoPagoParseadoInternalDto;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;

public interface ArchivoPagoService {

    ArchivoPagoParseadoInternalDto parsearArchivo(MultipartFile archivo);

    ArchivoPagoParseadoInternalDto parsearArchivoDesdeRuta(Path ruta);
}
