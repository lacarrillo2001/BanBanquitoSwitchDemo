package com.banquito.switchpagos.file.sftp;

import lombok.extern.slf4j.Slf4j;
import org.apache.sshd.server.session.ServerSession;
import org.apache.sshd.sftp.server.FileHandle;
import org.apache.sshd.sftp.server.Handle;
import org.apache.sshd.sftp.server.SftpEventListener;
import org.springframework.context.ApplicationEventPublisher;

import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

@Slf4j
public class SftpFileUploadListener implements SftpEventListener {

    private final ApplicationEventPublisher eventPublisher;

    public SftpFileUploadListener(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    @Override
    public void closed(ServerSession session, String remoteHandle, Handle localHandle, Throwable thrown) {
        if (thrown == null && localHandle instanceof FileHandle) {
            FileHandle fileHandle = (FileHandle) localHandle;
            Path path = fileHandle.getFile();
            
            // Verificamos si el archivo fue abierto para escritura o creación
            boolean wasWritten = fileHandle.getOpenOptions().contains(StandardOpenOption.WRITE) ||
                                 fileHandle.getOpenOptions().contains(StandardOpenOption.APPEND) ||
                                 fileHandle.getOpenOptions().contains(StandardOpenOption.CREATE) ||
                                 fileHandle.getOpenOptions().contains(StandardOpenOption.CREATE_NEW);

            if (wasWritten) {
                log.info("Archivo SFTP detectado: {}. Usuario: {}", path.getFileName(), session.getUsername());
                // Publicamos el evento para que el módulo de lote lo procese
                eventPublisher.publishEvent(new ArchivoSftpSubidoEvent(this, path));
            }
        }
    }
}
