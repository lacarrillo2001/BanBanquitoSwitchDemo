package com.banquito.switchpagos.file.sftp;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.apache.sshd.common.file.virtualfs.VirtualFileSystemFactory;
import org.apache.sshd.server.SshServer;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.apache.sshd.sftp.server.SftpSubsystemFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;

@Slf4j
@Component
public class SftpServerConfig {

    @Value("${sftp.port:2222}")
    private int port;

    @Value("${sftp.root-path:./sftp-data}")
    private String rootPath;

    @Value("${sftp.username:banquito}")
    private String username;

    @Value("${sftp.password:banquito123}")
    private String password;

    private SshServer sshd;
    private final ApplicationEventPublisher eventPublisher;

    public SftpServerConfig(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    @PostConstruct
    public void startServer() throws IOException {
        Path sftpRoot = Paths.get(rootPath).toAbsolutePath().normalize();
        if (!Files.exists(sftpRoot)) {
            Files.createDirectories(sftpRoot);
            log.info("Directorio SFTP creado en: {}", sftpRoot);
        }

        sshd = SshServer.setUpDefaultServer();
        sshd.setPort(port);
        sshd.setKeyPairProvider(new SimpleGeneratorHostKeyProvider(sftpRoot.resolve("hostkey.ser")));
        
        // Autenticación por Password (Simple para la demo)
        sshd.setPasswordAuthenticator((user, pass, session) -> 
            username.equals(user) && password.equals(pass)
        );

        // Configuración del Subsistema SFTP
        SftpSubsystemFactory factory = new SftpSubsystemFactory();
        
        // Agregamos un listener para detectar subidas
        factory.addSftpEventListener(new SftpFileUploadListener(eventPublisher));
        
        sshd.setSubsystemFactories(Collections.singletonList(factory));
        
        // Restringir el sistema de archivos al root path
        sshd.setFileSystemFactory(new VirtualFileSystemFactory(sftpRoot));

        sshd.start();
        log.info("Servidor SFTP iniciado en el puerto {}", port);
        log.info("Ruta raíz SFTP: {}", sftpRoot);
    }

    @PreDestroy
    public void stopServer() throws IOException {
        if (sshd != null && sshd.isStarted()) {
            sshd.stop();
            log.info("Servidor SFTP detenido.");
        }
    }
}
