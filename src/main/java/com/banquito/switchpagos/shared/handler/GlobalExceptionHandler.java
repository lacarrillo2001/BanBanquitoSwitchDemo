package com.banquito.switchpagos.shared.handler;

import com.banquito.switchpagos.shared.dto.api.ErrorDetalle;
import com.banquito.switchpagos.shared.dto.api.ErrorResponse;
import com.banquito.switchpagos.shared.exception.ConflictoOperacionException;
import com.banquito.switchpagos.shared.exception.EstadoInvalidoException;
import com.banquito.switchpagos.shared.exception.FormatoNoSoportadoException;
import com.banquito.switchpagos.shared.exception.IntegracionCoreException;
import com.banquito.switchpagos.shared.exception.RecursoNoEncontradoException;
import com.banquito.switchpagos.shared.exception.ReglaNegocioException;
import com.banquito.switchpagos.shared.exception.SolicitudInvalidaException;
import com.banquito.switchpagos.shared.exception.SwitchPagosException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Locale;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<ErrorResponse> manejarRecursoNoEncontrado(RecursoNoEncontradoException exception,
                                                                     HttpServletRequest request) {
        return construirRespuesta(exception, HttpStatus.NOT_FOUND, request, List.of());
    }

    @ExceptionHandler(SolicitudInvalidaException.class)
    public ResponseEntity<ErrorResponse> manejarSolicitudInvalida(SolicitudInvalidaException exception,
                                                                  HttpServletRequest request) {
        return construirRespuesta(exception, HttpStatus.BAD_REQUEST, request, List.of());
    }

    @ExceptionHandler(ReglaNegocioException.class)
    public ResponseEntity<ErrorResponse> manejarReglaNegocio(ReglaNegocioException exception,
                                                             HttpServletRequest request) {
        return construirRespuesta(exception, HttpStatus.UNPROCESSABLE_ENTITY, request, List.of());
    }

    @ExceptionHandler({EstadoInvalidoException.class, ConflictoOperacionException.class})
    public ResponseEntity<ErrorResponse> manejarConflicto(SwitchPagosException exception, HttpServletRequest request) {
        return construirRespuesta(exception, HttpStatus.CONFLICT, request, List.of());
    }

    @ExceptionHandler(IntegracionCoreException.class)
    public ResponseEntity<ErrorResponse> manejarIntegracionCore(IntegracionCoreException exception,
                                                                HttpServletRequest request) {
        return construirRespuesta(exception, HttpStatus.BAD_GATEWAY, request, List.of());
    }

    @ExceptionHandler(FormatoNoSoportadoException.class)
    public ResponseEntity<ErrorResponse> manejarFormatoNoSoportado(FormatoNoSoportadoException exception,
                                                                   HttpServletRequest request) {
        return construirRespuesta(exception, HttpStatus.UNSUPPORTED_MEDIA_TYPE, request, List.of());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> manejarCuerpoNoLegible(HttpMessageNotReadableException exception,
                                                                HttpServletRequest request) {
        SolicitudInvalidaException solicitudInvalidaException = new SolicitudInvalidaException(
                "SOLICITUD_NO_LEGIBLE",
                "El cuerpo de la solicitud no tiene un formato valido.",
                exception
        );
        return construirRespuesta(solicitudInvalidaException, HttpStatus.BAD_REQUEST, request, List.of());
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> manejarParametroFaltante(MissingServletRequestParameterException exception,
                                                                  HttpServletRequest request) {
        SolicitudInvalidaException solicitudInvalidaException = new SolicitudInvalidaException(
                "PARAMETRO_REQUERIDO",
                "Falta un parametro requerido en la solicitud.",
                exception
        );
        ErrorDetalle detalle = new ErrorDetalle(exception.getParameterName(), "El parametro es obligatorio.");
        return construirRespuesta(solicitudInvalidaException, HttpStatus.BAD_REQUEST, request, List.of(detalle));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> manejarTipoParametroInvalido(MethodArgumentTypeMismatchException exception,
                                                                      HttpServletRequest request) {
        SolicitudInvalidaException solicitudInvalidaException = new SolicitudInvalidaException(
                "PARAMETRO_INVALIDO",
                "Uno de los parametros de la solicitud tiene un tipo invalido.",
                exception
        );
        ErrorDetalle detalle = new ErrorDetalle(exception.getName(), "El valor enviado no tiene el tipo esperado.");
        return construirRespuesta(solicitudInvalidaException, HttpStatus.BAD_REQUEST, request, List.of(detalle));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> manejarViolacionIntegridad(DataIntegrityViolationException exception,
                                                                    HttpServletRequest request) {
        SolicitudInvalidaException solicitudInvalidaException = construirSolicitudInvalidaIntegridad(exception);
        return construirRespuesta(solicitudInvalidaException, HttpStatus.BAD_REQUEST, request, List.of());
    }

    private ResponseEntity<ErrorResponse> construirRespuesta(SwitchPagosException exception, HttpStatus status,
                                                             HttpServletRequest request, List<ErrorDetalle> detalles) {
        ErrorResponse response = new ErrorResponse(
                OffsetDateTime.now(),
                status.value(),
                status.name(),
                exception.getCodigo(),
                exception.getMessage(),
                request.getRequestURI(),
                detalles
        );
        return ResponseEntity.status(status).body(response);
    }

    private SolicitudInvalidaException construirSolicitudInvalidaIntegridad(DataIntegrityViolationException exception) {
        String mensajeTecnico = obtenerMensajeCausaRaiz(exception).toLowerCase(Locale.ROOT);
        if (mensajeTecnico.contains("chk_lote_totales_declarados")) {
            return new SolicitudInvalidaException(
                    "TOTALES_LOTE_INVALIDOS",
                    "El lote debe declarar al menos un registro y un monto total mayor a cero.",
                    exception
            );
        }
        if (mensajeTecnico.contains("chk_linea_monto")) {
            return new SolicitudInvalidaException(
                    "MONTO_LINEA_INVALIDO",
                    "Todas las lineas de pago deben tener un monto mayor a cero.",
                    exception
            );
        }
        if (mensajeTecnico.contains("uq_lote_uuid")
                || mensajeTecnico.contains("uq_lote_clave_idempotencia")
                || mensajeTecnico.contains("uq_linea_secuencial")
                || mensajeTecnico.contains("uq_linea_uuid_operacion")
                || mensajeTecnico.contains("duplicate key")) {
            return new SolicitudInvalidaException(
                    "REGISTRO_DUPLICADO",
                    "La solicitud intenta registrar informacion que ya existe.",
                    exception
            );
        }
        if (mensajeTecnico.contains("violates foreign key constraint")
                || mensajeTecnico.contains("viola la llave foranea")
                || mensajeTecnico.contains("viola la restriccion de llave foranea")) {
            return new SolicitudInvalidaException(
                    "REFERENCIA_INVALIDA",
                    "La solicitud contiene una referencia que no existe o no esta disponible.",
                    exception
            );
        }
        if (mensajeTecnico.contains("value too long")
                || mensajeTecnico.contains("valor es demasiado largo")) {
            return new SolicitudInvalidaException(
                    "LONGITUD_CAMPO_INVALIDA",
                    "Uno o mas campos exceden la longitud permitida.",
                    exception
            );
        }
        return new SolicitudInvalidaException(
                "DATOS_NO_CUMPLEN_REGLAS",
                "La solicitud contiene datos que no cumplen las reglas de integridad del sistema.",
                exception
        );
    }

    private String obtenerMensajeCausaRaiz(Throwable throwable) {
        Throwable causa = throwable;
        while (causa.getCause() != null) {
            causa = causa.getCause();
        }
        return causa.getMessage() != null ? causa.getMessage() : throwable.getMessage();
    }
}
