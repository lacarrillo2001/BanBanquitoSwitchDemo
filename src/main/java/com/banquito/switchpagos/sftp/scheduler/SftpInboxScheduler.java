package com.banquito.switchpagos.sftp.scheduler;

import com.banquito.switchpagos.sftp.config.SftpProperties;
import com.banquito.switchpagos.sftp.service.SftpArchivoService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Comparator;

@Component
@ConditionalOnProperty(name = "switch.sftp.enabled", havingValue = "true", matchIfMissing = true)
public class SftpInboxScheduler {

    private final SftpProperties properties;
    private final SftpArchivoService sftpArchivoService;

    public SftpInboxScheduler(SftpProperties properties, SftpArchivoService sftpArchivoService) {
        this.properties = properties;
        this.sftpArchivoService = sftpArchivoService;
    }

    @Scheduled(fixedDelayString = "${switch.sftp.scan-fixed-delay-ms:10000}")
    public void procesarInbox() throws IOException {
        Path root = Path.of(properties.getRootDirectory());
        Files.createDirectories(root);
        try (var paths = Files.walk(root, 2)) {
            paths.filter(Files::isRegularFile)
                    .filter(this::esArchivoProcesable)
                    .filter(this::estaEstable)
                    .sorted(Comparator.comparing(this::ultimoCambio))
                    .forEach(sftpArchivoService::procesarArchivo);
        }
    }

    private Boolean esArchivoProcesable(Path path) {
        String normalizado = path.getFileName().toString().toLowerCase();
        if (normalizado.equals("hostkey.ser") || normalizado.endsWith(".error.txt")) {
            return false;
        }
        if (path.toString().contains("processed") || path.toString().contains("error")) {
            return false;
        }
        return normalizado.endsWith(".csv") || normalizado.endsWith(".txt");
    }

    private Boolean estaEstable(Path path) {
        Instant limite = Instant.now().minusMillis(properties.getFileSettleMs());
        return ultimoCambio(path).isBefore(limite);
    }

    private Instant ultimoCambio(Path path) {
        try {
            return Files.getLastModifiedTime(path).toInstant();
        } catch (IOException exception) {
            return Instant.now();
        }
    }
}
