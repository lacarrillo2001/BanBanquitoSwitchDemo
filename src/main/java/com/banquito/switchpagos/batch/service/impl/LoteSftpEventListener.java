package com.banquito.switchpagos.batch.service.impl;

import com.banquito.switchpagos.batch.dto.internal.RegistroLoteInternalDto;
import com.banquito.switchpagos.batch.enums.CanalIngreso;
import com.banquito.switchpagos.batch.service.LotePagoService;
import com.banquito.switchpagos.file.dto.internal.ArchivoPagoParseadoInternalDto;
import com.banquito.switchpagos.file.service.ArchivoPagoService;
import com.banquito.switchpagos.file.sftp.ArchivoSftpSubidoEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class LoteSftpEventListener {

    private final LotePagoService lotePagoService;
    private final ArchivoPagoService archivoPagoService;

    public LoteSftpEventListener(LotePagoService lotePagoService, ArchivoPagoService archivoPagoService) {
        this.lotePagoService = lotePagoService;
        this.archivoPagoService = archivoPagoService;
    }

    @EventListener
    public void handleArchivoSftpSubido(ArchivoSftpSubidoEvent event) {
        log.info("Procesando archivo subido por SFTP: {}", event.getRutaArchivo());
        
        try {
            // Primero parseamos para obtener los datos de la cabecera (RUC, Tipo Servicio, Cuenta Matriz)
            ArchivoPagoParseadoInternalDto parsed = archivoPagoService.parsearArchivoDesdeRuta(event.getRutaArchivo());
            
            RegistroLoteInternalDto registroDto = new RegistroLoteInternalDto(
                    null, // No es MultipartFile
                    event.getRutaArchivo(),
                    parsed.cabecera().tipoServicio(),
                    parsed.cabecera().cuentaMatrizCargo(),
                    CanalIngreso.SFTP,
                    null, // No hay idCredencialWeb en SFTP
                    parsed.cabecera().rucEmpresa()
            );
            
            lotePagoService.registrarLote(registroDto);
            log.info("Lote registrado exitosamente desde SFTP para la empresa: {}", parsed.cabecera().rucEmpresa());
            
        } catch (Exception e) {
            log.error("Error al procesar el archivo SFTP: {}. Motivo: {}", event.getRutaArchivo().getFileName(), e.getMessage());
            // Aquí se podría mover el archivo a una carpeta de "errores"
        }
    }
}
