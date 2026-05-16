package com.banquito.switchpagos.sftp.service;

import java.nio.file.Path;

public interface SftpArchivoService {

    void procesarArchivo(Path archivo);
}
