package com.banquito.switchpagos.file.sftp;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.nio.file.Path;

@Getter
public class ArchivoSftpSubidoEvent extends ApplicationEvent {
    private final Path rutaArchivo;

    public ArchivoSftpSubidoEvent(Object source, Path rutaArchivo) {
        super(source);
        this.rutaArchivo = rutaArchivo;
    }
}
