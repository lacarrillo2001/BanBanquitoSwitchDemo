package com.banquito.switchpagos.file.service.impl;

import com.banquito.switchpagos.file.dto.internal.ArchivoPagoParseadoInternalDto;
import com.banquito.switchpagos.file.dto.internal.CabeceraArchivoPagoInternalDto;
import com.banquito.switchpagos.file.dto.internal.DetalleArchivoPagoInternalDto;
import com.banquito.switchpagos.file.dto.internal.PieArchivoPagoInternalDto;
import com.banquito.switchpagos.file.service.ArchivoPagoService;
import com.banquito.switchpagos.shared.exception.FormatoNoSoportadoException;
import com.banquito.switchpagos.shared.exception.SolicitudInvalidaException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;

@Service
public class ArchivoPagoServiceImpl implements ArchivoPagoService {

    @Override
    public ArchivoPagoParseadoInternalDto parsearArchivo(MultipartFile archivo) {
        validarArchivoRecibido(archivo);
        byte[] contenido = obtenerContenido(archivo);
        String textoArchivo = new String(contenido, StandardCharsets.UTF_8);
        String hashArchivo = calcularHashSha256(contenido);

        CabeceraArchivoPagoInternalDto cabecera = null;
        PieArchivoPagoInternalDto pie = null;
        List<DetalleArchivoPagoInternalDto> detalles = new ArrayList<>();

        String[] lineas = textoArchivo.split("\\R");
        for (Integer indice = 0; indice < lineas.length; indice++) {
            String linea = lineas[indice].trim();
            if (linea.isBlank()) {
                continue;
            }
            String[] campos = linea.split(",", -1);
            String tipoRegistro = campos[0].trim();
            if ("H".equals(tipoRegistro)) {
                cabecera = parsearCabecera(campos, indice + 1);
            } else if ("D".equals(tipoRegistro)) {
                detalles.add(parsearDetalle(campos, indice + 1));
            } else if ("T".equals(tipoRegistro)) {
                pie = parsearPie(campos, indice + 1);
            } else {
                throw new SolicitudInvalidaException(
                        "REGISTRO_ARCHIVO_DESCONOCIDO",
                        "La linea " + (indice + 1) + " tiene un tipo de registro no soportado."
                );
            }
        }

        return new ArchivoPagoParseadoInternalDto(
                archivo.getOriginalFilename(),
                hashArchivo,
                archivo.getSize(),
                cabecera,
                detalles,
                pie
        );
    }

    private void validarArchivoRecibido(MultipartFile archivo) {
        if (archivo == null || archivo.isEmpty()) {
            throw new SolicitudInvalidaException("ARCHIVO_REQUERIDO", "El archivo de pagos es obligatorio.");
        }
        String nombreArchivo = archivo.getOriginalFilename();
        if (nombreArchivo == null || nombreArchivo.isBlank()) {
            throw new SolicitudInvalidaException("NOMBRE_ARCHIVO_REQUERIDO", "El archivo debe tener nombre.");
        }
        String nombreNormalizado = nombreArchivo.toLowerCase();
        if (!nombreNormalizado.endsWith(".csv") && !nombreNormalizado.endsWith(".txt")) {
            throw new FormatoNoSoportadoException(
                    "FORMATO_ARCHIVO_NO_SOPORTADO",
                    "Solo se aceptan archivos CSV o TXT."
            );
        }
    }

    private byte[] obtenerContenido(MultipartFile archivo) {
        try {
            return archivo.getBytes();
        } catch (IOException exception) {
            throw new SolicitudInvalidaException(
                    "ARCHIVO_NO_LEGIBLE",
                    "No fue posible leer el archivo recibido.",
                    exception
            );
        }
    }

    private CabeceraArchivoPagoInternalDto parsearCabecera(String[] campos, Integer numeroLinea) {
        validarCantidadCampos(campos, 7, numeroLinea, "cabecera");
        return new CabeceraArchivoPagoInternalDto(
                campos[1].trim(),
                campos[2].trim(),
                parsearFechaHora(campos[3].trim(), numeroLinea),
                campos[4].trim(),
                parsearInteger(campos[5].trim(), numeroLinea, "total declarado"),
                parsearBigDecimal(campos[6].trim(), numeroLinea, "monto declarado")
        );
    }

    private DetalleArchivoPagoInternalDto parsearDetalle(String[] campos, Integer numeroLinea) {
        validarCantidadCampos(campos, 8, numeroLinea, "detalle");
        return new DetalleArchivoPagoInternalDto(
                parsearInteger(campos[1].trim(), numeroLinea, "secuencial"),
                campos[2].trim(),
                campos[3].trim(),
                campos[4].trim(),
                parsearBigDecimal(campos[5].trim(), numeroLinea, "monto"),
                campos[6].trim(),
                campos[7].trim()
        );
    }

    private PieArchivoPagoInternalDto parsearPie(String[] campos, Integer numeroLinea) {
        validarCantidadCampos(campos, 4, numeroLinea, "pie");
        return new PieArchivoPagoInternalDto(
                campos[1].trim(),
                parsearInteger(campos[2].trim(), numeroLinea, "total de pie"),
                parsearBigDecimal(campos[3].trim(), numeroLinea, "monto de pie")
        );
    }

    private void validarCantidadCampos(String[] campos, Integer cantidadEsperada, Integer numeroLinea,
                                       String tipoRegistro) {
        if (campos.length != cantidadEsperada) {
            throw new SolicitudInvalidaException(
                    "ESTRUCTURA_ARCHIVO_INVALIDA",
                    "La linea " + numeroLinea + " de " + tipoRegistro + " no tiene la cantidad de campos esperada."
            );
        }
    }

    private Integer parsearInteger(String valor, Integer numeroLinea, String campo) {
        try {
            return Integer.valueOf(valor);
        } catch (NumberFormatException exception) {
            throw new SolicitudInvalidaException(
                    "ENTERO_ARCHIVO_INVALIDO",
                    "La linea " + numeroLinea + " tiene un valor invalido para " + campo + ".",
                    exception
            );
        }
    }

    private BigDecimal parsearBigDecimal(String valor, Integer numeroLinea, String campo) {
        try {
            return new BigDecimal(valor);
        } catch (NumberFormatException exception) {
            throw new SolicitudInvalidaException(
                    "MONTO_ARCHIVO_INVALIDO",
                    "La linea " + numeroLinea + " tiene un valor invalido para " + campo + ".",
                    exception
            );
        }
    }

    private OffsetDateTime parsearFechaHora(String valor, Integer numeroLinea) {
        try {
            return OffsetDateTime.parse(valor);
        } catch (DateTimeParseException exception) {
            throw new SolicitudInvalidaException(
                    "FECHA_ARCHIVO_INVALIDA",
                    "La linea " + numeroLinea + " tiene una fecha de generacion invalida.",
                    exception
            );
        }
    }

    private String calcularHashSha256(byte[] contenido) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(messageDigest.digest(contenido));
        } catch (NoSuchAlgorithmException exception) {
            throw new SolicitudInvalidaException(
                    "HASH_ARCHIVO_NO_CALCULADO",
                    "No fue posible calcular el hash del archivo.",
                    exception
            );
        }
    }
}
