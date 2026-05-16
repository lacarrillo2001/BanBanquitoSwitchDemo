package com.banquito.switchpagos.sftp.server;

import com.banquito.switchpagos.integrationcore.dto.internal.AutenticacionCoreResponse;
import com.banquito.switchpagos.integrationcore.service.CoreBancarioService;
import com.banquito.switchpagos.sftp.config.SftpProperties;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.apache.sshd.common.file.virtualfs.VirtualFileSystemFactory;
import org.apache.sshd.server.SshServer;
import org.apache.sshd.server.auth.password.PasswordAuthenticator;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.apache.sshd.sftp.server.SftpSubsystemFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@Component
@ConditionalOnProperty(name = "switch.sftp.enabled", havingValue = "true", matchIfMissing = true)
public class EmbeddedSftpServer {

    private static final Logger LOGGER = LoggerFactory.getLogger(EmbeddedSftpServer.class);
    private static final String ROL_EMPRESA_PAGOS_MASIVOS = "EMPRESA_PAGOS_MASIVOS";

    private final SftpProperties properties;
    private final CoreBancarioService coreBancarioService;
    private SshServer sshServer;

    public EmbeddedSftpServer(SftpProperties properties, CoreBancarioService coreBancarioService) {
        this.properties = properties;
        this.coreBancarioService = coreBancarioService;
    }

    @PostConstruct
    public void start() {
        try {
            Path root = Path.of(properties.getRootDirectory()).toAbsolutePath().normalize();
            Files.createDirectories(root);

            sshServer = SshServer.setUpDefaultServer();
            sshServer.setHost(properties.getHost());
            sshServer.setPort(properties.getPort());
            sshServer.setKeyPairProvider(new SimpleGeneratorHostKeyProvider(root.resolve("hostkey.ser")));
            sshServer.setPasswordAuthenticator(passwordAuthenticator());
            sshServer.setFileSystemFactory(new VirtualFileSystemFactory(root));
            sshServer.setSubsystemFactories(List.of(new SftpSubsystemFactory.Builder().build()));
            sshServer.start();
            LOGGER.info("Servidor SFTP embebido iniciado en {}:{} con raiz {}",
                    properties.getHost(),
                    properties.getPort(),
                    root
            );
        } catch (IOException | RuntimeException exception) {
            sshServer = null;
            LOGGER.error("No se pudo iniciar el servidor SFTP embebido en {}:{}.",
                    properties.getHost(),
                    properties.getPort(),
                    exception
            );
            if (Boolean.TRUE.equals(properties.getFailOnStartError())) {
                throw new IllegalStateException("No se pudo iniciar el servidor SFTP embebido.", exception);
            }
        }
    }

    @PreDestroy
    public void stop() throws IOException {
        if (sshServer != null && sshServer.isOpen()) {
            sshServer.stop();
        }
    }

    private PasswordAuthenticator passwordAuthenticator() {
        return (username, password, session) -> {
            try {
                AutenticacionCoreResponse response = coreBancarioService.autenticar(username, password);
                if (!Boolean.TRUE.equals(response.autenticado())) {
                    return false;
                }
                if (!ROL_EMPRESA_PAGOS_MASIVOS.equals(response.rolSwitch())) {
                    return false;
                }
                return Boolean.TRUE.equals(response.activoPagosMasivos());
            } catch (RuntimeException exception) {
                LOGGER.warn("Autenticacion SFTP rechazada para usuario {} por error al consultar Core.", username, exception);
                return false;
            }
        };
    }
}
