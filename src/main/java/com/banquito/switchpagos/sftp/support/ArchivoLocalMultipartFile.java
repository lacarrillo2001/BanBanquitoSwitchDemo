package com.banquito.switchpagos.sftp.support;

import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class ArchivoLocalMultipartFile implements MultipartFile {

    private final Path path;
    private final byte[] contenido;

    public ArchivoLocalMultipartFile(Path path) throws IOException {
        this.path = path;
        this.contenido = Files.readAllBytes(path);
    }

    @Override
    public String getName() {
        return path.getFileName().toString();
    }

    @Override
    public String getOriginalFilename() {
        return path.getFileName().toString();
    }

    @Override
    public String getContentType() {
        return "text/plain";
    }

    @Override
    public boolean isEmpty() {
        return contenido.length == 0;
    }

    @Override
    public long getSize() {
        return contenido.length;
    }

    @Override
    public byte[] getBytes() {
        return contenido.clone();
    }

    @Override
    public InputStream getInputStream() {
        return new ByteArrayInputStream(contenido);
    }

    @Override
    public void transferTo(Path dest) throws IOException {
        Files.write(dest, contenido);
    }

    @Override
    public void transferTo(File dest) throws IOException {
        Files.write(dest.toPath(), contenido);
    }
}
