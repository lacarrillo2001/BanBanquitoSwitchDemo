package com.banquito.switchpagos.sftp.service.impl;

import com.banquito.switchpagos.batch.dto.internal.RegistroLoteInternalDto;
import com.banquito.switchpagos.batch.enums.CanalIngreso;
import com.banquito.switchpagos.batch.service.LotePagoService;
import com.banquito.switchpagos.sftp.config.SftpProperties;
import com.banquito.switchpagos.sftp.service.SftpArchivoService;
import com.banquito.switchpagos.sftp.support.ArchivoLocalMultipartFile;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class SftpArchivoServiceImpl implements SftpArchivoService {

    private final LotePagoService lotePagoService;
    private final SftpProperties sftpProperties;

    public SftpArchivoServiceImpl(LotePagoService lotePagoService, SftpProperties sftpProperties) {
        this.lotePagoService = lotePagoService;
        this.sftpProperties = sftpProperties;
    }

    @Override
    public void procesarArchivo(Path archivo) {
        try {
            CabeceraSftp cabecera = leerCabecera(archivo);
            lotePagoService.registrarLote(new RegistroLoteInternalDto(
                    new ArchivoLocalMultipartFile(archivo),
                    cabecera.tipoServicio(),
                    cabecera.cuentaMatrizCargo(),
                    CanalIngreso.SFTP,
                    null,
                    cabecera.usernameCredencialWebCore(),
                    cabecera.rucEmpresa()
            ));
            moverArchivo(archivo, "processed");
        } catch (Exception exception) {
            moverArchivoConError(archivo, exception);
        }
    }

    private CabeceraSftp leerCabecera(Path archivo) throws IOException {
        String primeraCabecera = Files.readAllLines(archivo, StandardCharsets.UTF_8)
                .stream()
                .map(String::trim)
                .filter(linea -> !linea.isBlank())
                .filter(linea -> linea.startsWith("H,"))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("El archivo SFTP no contiene cabecera H."));

        String[] campos = primeraCabecera.split(",", -1);
        if (campos.length != 7) {
            throw new IllegalArgumentException("La cabecera H del archivo SFTP no tiene 7 campos.");
        }
        String username = obtenerUsernameDesdeRuta(archivo);
        return new CabeceraSftp(
                campos[1].trim(),
                campos[2].trim(),
                campos[4].trim(),
                username
        );
    }

    private String obtenerUsernameDesdeRuta(Path archivo) {
        Path root = Path.of(sftpProperties.getRootDirectory()).toAbsolutePath().normalize();
        Path absoluto = archivo.toAbsolutePath().normalize();
        if (absoluto.getParent() != null && !absoluto.getParent().equals(root)) {
            return absoluto.getParent().getFileName().toString();
        }
        return null;
    }

    private void moverArchivo(Path archivo, String subdirectorio) throws IOException {
        Path destinoDirectorio = Path.of(sftpProperties.getRootDirectory(), subdirectorio);
        Files.createDirectories(destinoDirectorio);
        Files.move(
                archivo,
                destinoDirectorio.resolve(nombreConTimestamp(archivo)),
                StandardCopyOption.REPLACE_EXISTING
        );
    }

    private void moverArchivoConError(Path archivo, Exception exception) {
        try {
            Path destinoDirectorio = Path.of(sftpProperties.getRootDirectory(), "error");
            Files.createDirectories(destinoDirectorio);
            Path destino = destinoDirectorio.resolve(nombreConTimestamp(archivo));
            Files.move(archivo, destino, StandardCopyOption.REPLACE_EXISTING);
            Files.writeString(
                    destinoDirectorio.resolve(destino.getFileName() + ".error.txt"),
                    exception.getMessage() != null ? exception.getMessage() : exception.getClass().getName(),
                    StandardCharsets.UTF_8
            );
        } catch (IOException ignored) {
            // Si falla el movimiento a error, el scheduler volvera a intentar en el siguiente ciclo.
        }
    }

    private String nombreConTimestamp(Path archivo) {
        String timestamp = OffsetDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"));
        return timestamp + "-" + archivo.getFileName();
    }

    private record CabeceraSftp(
            String rucEmpresa,
            String tipoServicio,
            String cuentaMatrizCargo,
            String usernameCredencialWebCore
    ) {
    }
}
