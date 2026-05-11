package com.banquito.switchpagos.common.handler;

import com.banquito.switchpagos.common.dto.api.ErrorDetalle;
import com.banquito.switchpagos.common.dto.api.ErrorResponse;
import com.banquito.switchpagos.common.exception.ConflictoOperacionException;
import com.banquito.switchpagos.common.exception.EstadoInvalidoException;
import com.banquito.switchpagos.common.exception.FormatoNoSoportadoException;
import com.banquito.switchpagos.common.exception.IntegracionCoreException;
import com.banquito.switchpagos.common.exception.RecursoNoEncontradoException;
import com.banquito.switchpagos.common.exception.ReglaNegocioException;
import com.banquito.switchpagos.common.exception.SolicitudInvalidaException;
import com.banquito.switchpagos.common.exception.SwitchPagosException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.OffsetDateTime;
import java.util.List;

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
}
