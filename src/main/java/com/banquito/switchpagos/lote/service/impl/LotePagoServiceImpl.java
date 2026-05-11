package com.banquito.switchpagos.lote.service.impl;

import com.banquito.switchpagos.archivo.dto.internal.ArchivoPagoParseadoInternalDto;
import com.banquito.switchpagos.archivo.dto.internal.ErrorValidacionArchivoInternalDto;
import com.banquito.switchpagos.archivo.dto.internal.ResultadoValidacionArchivoInternalDto;
import com.banquito.switchpagos.archivo.service.ArchivoPagoService;
import com.banquito.switchpagos.archivo.service.ValidadorArchivoPagoService;
import com.banquito.switchpagos.auditoria.dto.internal.RegistroAuditoriaRequest;
import com.banquito.switchpagos.auditoria.enums.TipoActorAuditoria;
import com.banquito.switchpagos.auditoria.service.AuditoriaSwitchService;
import com.banquito.switchpagos.catalogo.model.TipoServicio;
import com.banquito.switchpagos.catalogo.service.TipoServicioService;
import com.banquito.switchpagos.common.exception.ConflictoOperacionException;
import com.banquito.switchpagos.common.exception.EstadoInvalidoException;
import com.banquito.switchpagos.common.exception.RecursoNoEncontradoException;
import com.banquito.switchpagos.common.exception.ReglaNegocioException;
import com.banquito.switchpagos.common.exception.SolicitudInvalidaException;
import com.banquito.switchpagos.lote.dto.api.AnulacionLoteResponse;
import com.banquito.switchpagos.lote.dto.api.CargaLoteResponse;
import com.banquito.switchpagos.lote.dto.api.ConsultaLoteResponse;
import com.banquito.switchpagos.lote.dto.api.ErrorGlobalResponse;
import com.banquito.switchpagos.lote.dto.api.EstadoLoteResponse;
import com.banquito.switchpagos.lote.dto.api.FechasLoteResponse;
import com.banquito.switchpagos.lote.dto.api.LineaPagoResponse;
import com.banquito.switchpagos.lote.dto.api.PaginaResponse;
import com.banquito.switchpagos.lote.dto.api.ResumenEstadoLoteResponse;
import com.banquito.switchpagos.lote.dto.api.TotalesValidacionResponse;
import com.banquito.switchpagos.lote.dto.api.ValidacionLoteResponse;
import com.banquito.switchpagos.lote.dto.internal.LoteProcesamientoInternalDto;
import com.banquito.switchpagos.lote.dto.internal.RegistroLoteInternalDto;
import com.banquito.switchpagos.lote.enums.EstadoColaProcesamiento;
import com.banquito.switchpagos.lote.enums.EstadoLote;
import com.banquito.switchpagos.lote.enums.FormatoArchivo;
import com.banquito.switchpagos.lote.model.ColaProcesamiento;
import com.banquito.switchpagos.lote.model.HistorialEstadoLote;
import com.banquito.switchpagos.lote.model.LotePago;
import com.banquito.switchpagos.lote.repository.ColaProcesamientoRepository;
import com.banquito.switchpagos.lote.repository.HistorialEstadoLoteRepository;
import com.banquito.switchpagos.lote.repository.LotePagoRepository;
import com.banquito.switchpagos.lote.service.LotePagoService;
import com.banquito.switchpagos.parametro.constants.CodigoParametroSwitch;
import com.banquito.switchpagos.parametro.service.ParametroSwitchService;
import com.banquito.switchpagos.procesamiento.dto.internal.LineaPagoInternalDto;
import com.banquito.switchpagos.procesamiento.enums.EstadoLineaPago;
import com.banquito.switchpagos.procesamiento.service.LineaPagoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class LotePagoServiceImpl implements LotePagoService {

    private static final ZoneId ZONA_HORARIA_OPERATIVA = ZoneId.of("America/Guayaquil");

    private final LotePagoRepository lotePagoRepository;
    private final HistorialEstadoLoteRepository historialEstadoLoteRepository;
    private final ColaProcesamientoRepository colaProcesamientoRepository;
    private final ArchivoPagoService archivoPagoService;
    private final ValidadorArchivoPagoService validadorArchivoPagoService;
    private final ParametroSwitchService parametroSwitchService;
    private final TipoServicioService tipoServicioService;
    private final LineaPagoService lineaPagoService;
    private final AuditoriaSwitchService auditoriaSwitchService;
    private final ObjectMapper objectMapper;

    public LotePagoServiceImpl(LotePagoRepository lotePagoRepository,
                               HistorialEstadoLoteRepository historialEstadoLoteRepository,
                               ColaProcesamientoRepository colaProcesamientoRepository,
                               ArchivoPagoService archivoPagoService,
                               ValidadorArchivoPagoService validadorArchivoPagoService,
                               ParametroSwitchService parametroSwitchService,
                               TipoServicioService tipoServicioService,
                               LineaPagoService lineaPagoService,
                               AuditoriaSwitchService auditoriaSwitchService,
                               ObjectMapper objectMapper) {
        this.lotePagoRepository = lotePagoRepository;
        this.historialEstadoLoteRepository = historialEstadoLoteRepository;
        this.colaProcesamientoRepository = colaProcesamientoRepository;
        this.archivoPagoService = archivoPagoService;
        this.validadorArchivoPagoService = validadorArchivoPagoService;
        this.parametroSwitchService = parametroSwitchService;
        this.tipoServicioService = tipoServicioService;
        this.lineaPagoService = lineaPagoService;
        this.auditoriaSwitchService = auditoriaSwitchService;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional
    public CargaLoteResponse registrarLote(RegistroLoteInternalDto registroLoteInternalDto) {
        ArchivoPagoParseadoInternalDto archivoPagoParseado = archivoPagoService.parsearArchivo(registroLoteInternalDto.archivo());
        ResultadoValidacionArchivoInternalDto resultadoValidacion = validadorArchivoPagoService.validarEstructura(archivoPagoParseado);
        if (!resultadoValidacion.valido()) {
            ErrorValidacionArchivoInternalDto primerError = resultadoValidacion.errores().getFirst();
            throw new SolicitudInvalidaException(primerError.codigo(), primerError.mensaje());
        }
        validarDatosSolicitud(registroLoteInternalDto, archivoPagoParseado);
        validarDuplicidad(archivoPagoParseado.cabecera().rucEmpresa(), archivoPagoParseado.nombreArchivo(),
                archivoPagoParseado.hashArchivo());

        OffsetDateTime fechaRecepcion = OffsetDateTime.now(ZONA_HORARIA_OPERATIVA);
        EstadoLote estadoInicial = calcularEstadoInicial(fechaRecepcion);
        LotePago lotePago = construirLotePago(registroLoteInternalDto, archivoPagoParseado, estadoInicial, fechaRecepcion);
        lotePagoRepository.save(lotePago);
        lineaPagoService.guardarLineasPendientes(lotePago, archivoPagoParseado.detalles());
        registrarHistorialEstado(lotePago, null, estadoInicial, "Registro inicial del lote.", "SISTEMA");
        if (EstadoLote.ENCOLADO.equals(estadoInicial)) {
            registrarColaProcesamiento(lotePago, fechaRecepcion);
        }
        registrarAuditoria(lotePago, "CREACION_LOTE", null, construirDatosBasicos(lotePago));
        return new CargaLoteResponse(
                lotePago.getUuidLote(),
                lotePago.getEstado().name(),
                lotePago.getNombreArchivo(),
                lotePago.getHashArchivo(),
                "Lote registrado correctamente."
        );
    }

    @Override
    @Transactional(readOnly = true)
    public PaginaResponse<ConsultaLoteResponse> consultarLotes(String rucEmpresa, EstadoLote estado, String tipoServicio,
                                                               OffsetDateTime fechaDesde, OffsetDateTime fechaHasta,
                                                               Pageable pageable) {
        TipoServicio tipoServicioFiltro = tipoServicio == null || tipoServicio.isBlank()
                ? null
                : new TipoServicio(tipoServicio);
        Page<ConsultaLoteResponse> pagina = lotePagoRepository.consultarPorFiltros(
                rucEmpresa,
                estado,
                tipoServicioFiltro,
                fechaDesde,
                fechaHasta,
                pageable
        ).map(this::construirConsultaLoteResponse);
        return construirPaginaResponse(pagina);
    }

    @Override
    @Transactional(readOnly = true)
    public EstadoLoteResponse consultarEstado(UUID uuidLote) {
        LotePago lotePago = obtenerLotePorUuid(uuidLote);
        return construirEstadoLoteResponse(lotePago);
    }

    @Override
    @Transactional
    public AnulacionLoteResponse anularLote(UUID uuidLote, String motivo) {
        LotePago lotePago = obtenerLotePorUuid(uuidLote);
        if (!esEstadoAnulable(lotePago.getEstado())) {
            throw new EstadoInvalidoException(
                    "LOTE_NO_ANULABLE",
                    "El lote no puede anularse en estado " + lotePago.getEstado() + "."
            );
        }
        EstadoLote estadoAnterior = lotePago.getEstado();
        lotePago.setEstado(EstadoLote.ANULADO);
        lotePago.setMotivoRechazoGlobal(motivo);
        lotePago.setFechaActualizacion(OffsetDateTime.now(ZONA_HORARIA_OPERATIVA));
        lotePagoRepository.save(lotePago);
        registrarHistorialEstado(lotePago, estadoAnterior, EstadoLote.ANULADO, motivo, "SISTEMA");
        registrarAuditoria(lotePago, "ANULACION_LOTE", construirEstadoAuditoria(estadoAnterior), construirDatosBasicos(lotePago));
        return new AnulacionLoteResponse(lotePago.getUuidLote(), lotePago.getEstado().name(), motivo);
    }

    @Override
    @Transactional
    public ValidacionLoteResponse validarLote(UUID uuidLote) {
        LotePago lotePago = obtenerLotePorUuid(uuidLote);
        if (!EstadoLote.RECIBIDO.equals(lotePago.getEstado()) && !EstadoLote.ENCOLADO.equals(lotePago.getEstado())) {
            throw new EstadoInvalidoException(
                    "LOTE_ESTADO_NO_VALIDABLE",
                    "Solo se pueden validar lotes en estado RECIBIDO o ENCOLADO."
            );
        }
        EstadoLote estadoAnterior = lotePago.getEstado();
        cambiarEstado(lotePago, EstadoLote.VALIDANDO, "Inicio de validacion estructural.", "SISTEMA");
        lotePago.setFechaInicioValidacion(OffsetDateTime.now(ZONA_HORARIA_OPERATIVA));

        List<ErrorGlobalResponse> errores = validarReglasLote(lotePago);
        TotalesValidacionResponse totales = construirTotalesValidacion(lotePago);
        if (errores.isEmpty()) {
            lotePago.setTotalRegistrosValidados(totales.totalLineasParseadas().intValue());
            lotePago.setTotalRegistrosRechazados(0);
            lotePago.setMontoTotalValidado(totales.montoTotalDetalle());
            lotePago.setFechaFinValidacion(OffsetDateTime.now(ZONA_HORARIA_OPERATIVA));
            cambiarEstado(lotePago, EstadoLote.VALIDADO, "Validacion estructural exitosa.", "SISTEMA");
            registrarAuditoria(lotePago, "VALIDACION_LOTE_EXITOSA", construirEstadoAuditoria(estadoAnterior),
                    construirDatosBasicos(lotePago));
            return new ValidacionLoteResponse(lotePago.getUuidLote(), lotePago.getEstado().name(), Boolean.TRUE,
                    totales, errores);
        }

        String motivoRechazo = errores.getFirst().mensaje();
        lotePago.setMotivoRechazoGlobal(motivoRechazo);
        lotePago.setFechaFinValidacion(OffsetDateTime.now(ZONA_HORARIA_OPERATIVA));
        cambiarEstado(lotePago, EstadoLote.RECHAZADO, motivoRechazo, "SISTEMA");
        registrarAuditoria(lotePago, "VALIDACION_LOTE_RECHAZADA", construirEstadoAuditoria(estadoAnterior),
                construirDatosBasicos(lotePago));
        return new ValidacionLoteResponse(lotePago.getUuidLote(), lotePago.getEstado().name(), Boolean.FALSE,
                totales, errores);
    }

    @Override
    @Transactional(readOnly = true)
    public PaginaResponse<LineaPagoResponse> consultarLineas(UUID uuidLote, EstadoLineaPago estado, Pageable pageable) {
        LotePago lotePago = obtenerLotePorUuid(uuidLote);
        Page<LineaPagoResponse> pagina = lineaPagoService.consultarLineas(lotePago, estado, pageable)
                .map(this::construirLineaPagoResponse);
        return construirPaginaResponse(pagina);
    }

    @Override
    @Transactional(readOnly = true)
    public LoteProcesamientoInternalDto obtenerDatosProcesamiento(UUID uuidLote) {
        LotePago lotePago = obtenerLotePorUuid(uuidLote);
        return construirLoteProcesamientoInternalDto(lotePago);
    }

    @Override
    @Transactional
    public void iniciarProcesamiento(UUID uuidLote, String ejecutadoPor) {
        LotePago lotePago = obtenerLotePorUuid(uuidLote);
        if (!EstadoLote.VALIDADO.equals(lotePago.getEstado())) {
            throw new EstadoInvalidoException(
                    "LOTE_ESTADO_NO_PROCESABLE",
                    "Solo se pueden procesar lotes en estado VALIDADO."
            );
        }
        lotePago.setFechaInicioProceso(OffsetDateTime.now(ZONA_HORARIA_OPERATIVA));
        cambiarEstado(lotePago, EstadoLote.PROCESANDO, "Inicio de procesamiento financiero.", ejecutadoPor);
        registrarAuditoria(lotePago, "INICIO_PROCESAMIENTO_LOTE", construirEstadoAuditoria(EstadoLote.VALIDADO),
                construirDatosBasicos(lotePago));
    }

    @Override
    @Transactional
    public void finalizarProcesamiento(UUID uuidLote, EstadoLote estadoFinal, Integer totalValidadas,
                                       Integer totalRechazadas, BigDecimal montoTotalValidado,
                                       String ejecutadoPor) {
        LotePago lotePago = obtenerLotePorUuid(uuidLote);
        EstadoLote estadoAnterior = lotePago.getEstado();
        lotePago.setTotalRegistrosValidados(totalValidadas);
        lotePago.setTotalRegistrosRechazados(totalRechazadas);
        lotePago.setMontoTotalValidado(montoTotalValidado);
        lotePago.setFechaFinProceso(OffsetDateTime.now(ZONA_HORARIA_OPERATIVA));
        cambiarEstado(lotePago, estadoFinal, "Fin de procesamiento financiero linea por linea.", ejecutadoPor);
        registrarAuditoria(lotePago, "FIN_PROCESAMIENTO_LOTE", construirEstadoAuditoria(estadoAnterior),
                construirDatosBasicos(lotePago));
    }

    @Override
    @Transactional
    public void cerrarLoteLiquidado(UUID uuidLote, String ejecutadoPor) {
        LotePago lotePago = obtenerLotePorUuid(uuidLote);
        EstadoLote estadoAnterior = lotePago.getEstado();
        lotePago.setFechaCierre(OffsetDateTime.now(ZONA_HORARIA_OPERATIVA));
        cambiarEstado(lotePago, EstadoLote.CERRADO, "Liquidacion contable completada.", ejecutadoPor);
        registrarAuditoria(lotePago, "CIERRE_LOTE_LIQUIDADO", construirEstadoAuditoria(estadoAnterior),
                construirDatosBasicos(lotePago));
    }

    private void validarDatosSolicitud(RegistroLoteInternalDto registroLoteInternalDto,
                                       ArchivoPagoParseadoInternalDto archivoPagoParseado) {
        if (!archivoPagoParseado.cabecera().tipoServicio().equals(registroLoteInternalDto.tipoServicio())) {
            throw new SolicitudInvalidaException(
                    "TIPO_SERVICIO_NO_COINCIDE",
                    "El tipo de servicio enviado no coincide con la cabecera del archivo."
            );
        }
        if (!archivoPagoParseado.cabecera().cuentaMatrizCargo().equals(registroLoteInternalDto.cuentaMatrizCargo())) {
            throw new SolicitudInvalidaException(
                    "CUENTA_MATRIZ_NO_COINCIDE",
                    "La cuenta matriz enviada no coincide con la cabecera del archivo."
            );
        }
        if (registroLoteInternalDto.rucEmpresa() != null
                && !registroLoteInternalDto.rucEmpresa().isBlank()
                && !archivoPagoParseado.cabecera().rucEmpresa().equals(registroLoteInternalDto.rucEmpresa())) {
            throw new SolicitudInvalidaException(
                    "RUC_EMPRESA_NO_COINCIDE",
                    "El RUC enviado no coincide con la cabecera del archivo."
            );
        }
    }

    private void validarDuplicidad(String rucEmpresa, String nombreArchivo, String hashArchivo) {
        Integer ventanaDuplicidadDias = parametroSwitchService.obtenerInteger(CodigoParametroSwitch.VENTANA_DUPLICIDAD_DIAS);
        OffsetDateTime fechaLimite = OffsetDateTime.now(ZONA_HORARIA_OPERATIVA).minusDays(ventanaDuplicidadDias);
        Boolean existeDuplicado = lotePagoRepository.existsByRucEmpresaAndNombreArchivoAndHashArchivoAndFechaRecepcionAfter(
                rucEmpresa,
                nombreArchivo,
                hashArchivo,
                fechaLimite
        );
        if (Boolean.TRUE.equals(existeDuplicado)) {
            throw new ConflictoOperacionException(
                    "LOTE_DUPLICADO",
                    "Ya existe un lote recibido con el mismo RUC, nombre de archivo y hash dentro de la ventana configurada."
            );
        }
    }

    private LotePago construirLotePago(RegistroLoteInternalDto registroLoteInternalDto,
                                       ArchivoPagoParseadoInternalDto archivoPagoParseado,
                                       EstadoLote estadoInicial,
                                       OffsetDateTime fechaRecepcion) {
        LotePago lotePago = new LotePago();
        lotePago.setUuidLote(UUID.randomUUID());
        lotePago.setClaveIdempotencia(UUID.randomUUID());
        lotePago.setRucEmpresa(archivoPagoParseado.cabecera().rucEmpresa());
        lotePago.setIdCredencialWebCore(registroLoteInternalDto.idCredencialWebCore());
        lotePago.setTipoServicio(new TipoServicio(archivoPagoParseado.cabecera().tipoServicio()));
        lotePago.setCuentaMatrizCargo(archivoPagoParseado.cabecera().cuentaMatrizCargo());
        lotePago.setFechaHoraGeneracion(archivoPagoParseado.cabecera().fechaHoraGeneracion());
        lotePago.setTotalRegistrosDeclarado(archivoPagoParseado.cabecera().totalRegistrosDeclarado());
        lotePago.setMontoTotalDeclarado(archivoPagoParseado.cabecera().montoTotalDeclarado());
        lotePago.setTotalRegistrosPie(archivoPagoParseado.pie().totalRegistrosPie());
        lotePago.setMontoTotalPie(archivoPagoParseado.pie().montoTotalPie());
        lotePago.setNombreArchivo(archivoPagoParseado.nombreArchivo());
        lotePago.setHashArchivo(archivoPagoParseado.hashArchivo());
        lotePago.setHashPieControl(archivoPagoParseado.pie().hashPieControl());
        lotePago.setTamanoBytes(archivoPagoParseado.tamanoBytes());
        lotePago.setFormatoArchivo(obtenerFormatoArchivo(archivoPagoParseado.nombreArchivo()));
        lotePago.setCanalIngreso(registroLoteInternalDto.canalIngreso());
        lotePago.setEstado(estadoInicial);
        lotePago.setFechaRecepcion(fechaRecepcion);
        return lotePago;
    }

    private EstadoLote calcularEstadoInicial(OffsetDateTime fechaRecepcion) {
        LocalTime horaCorte = parametroSwitchService.obtenerHora(CodigoParametroSwitch.HORA_CORTE_PROCESO);
        LocalDate fechaLocal = fechaRecepcion.toLocalDate();
        if (esDiaHabil(fechaLocal) && fechaRecepcion.toLocalTime().isBefore(horaCorte)) {
            return EstadoLote.RECIBIDO;
        }
        return EstadoLote.ENCOLADO;
    }

    private Boolean esDiaHabil(LocalDate fecha) {
        return !DayOfWeek.SATURDAY.equals(fecha.getDayOfWeek()) && !DayOfWeek.SUNDAY.equals(fecha.getDayOfWeek());
    }

    private void registrarColaProcesamiento(LotePago lotePago, OffsetDateTime fechaRecepcion) {
        LocalTime horaInicio = parametroSwitchService.obtenerHora(CodigoParametroSwitch.HORA_INICIO_LOTES_ENCOLADOS);
        LocalDate siguienteDiaHabil = obtenerSiguienteDiaHabil(fechaRecepcion.toLocalDate());
        ColaProcesamiento colaProcesamiento = new ColaProcesamiento();
        colaProcesamiento.setLotePago(lotePago);
        colaProcesamiento.setFechaHabilProgramada(siguienteDiaHabil);
        colaProcesamiento.setFechaEncolado(fechaRecepcion);
        colaProcesamiento.setFechaProgramadaProceso(siguienteDiaHabil.atTime(horaInicio).atZone(ZONA_HORARIA_OPERATIVA).toOffsetDateTime());
        colaProcesamiento.setEstadoCola(EstadoColaProcesamiento.PENDIENTE);
        colaProcesamiento.setPrioridad(5);
        colaProcesamiento.setIntentos(0);
        colaProcesamiento.setMaxIntentos(parametroSwitchService.obtenerInteger(CodigoParametroSwitch.MAX_REINTENTOS_LOTE));
        colaProcesamientoRepository.save(colaProcesamiento);
    }

    private LocalDate obtenerSiguienteDiaHabil(LocalDate fechaBase) {
        LocalDate fechaProgramada = fechaBase.plusDays(1);
        while (!esDiaHabil(fechaProgramada)) {
            fechaProgramada = fechaProgramada.plusDays(1);
        }
        return fechaProgramada;
    }

    private List<ErrorGlobalResponse> validarReglasLote(LotePago lotePago) {
        List<ErrorGlobalResponse> errores = new ArrayList<>();
        if (!tipoServicioService.existeActivo(lotePago.getTipoServicio().getCodigo())) {
            errores.add(new ErrorGlobalResponse("TIPO_SERVICIO_INACTIVO", "El tipo de servicio no existe o no esta activo."));
        }
        Integer ventanaDuplicidadDias = parametroSwitchService.obtenerInteger(CodigoParametroSwitch.VENTANA_DUPLICIDAD_DIAS);
        OffsetDateTime fechaLimite = OffsetDateTime.now(ZONA_HORARIA_OPERATIVA).minusDays(ventanaDuplicidadDias);
        Boolean existeDuplicado = lotePagoRepository
                .existsByRucEmpresaAndNombreArchivoAndHashArchivoAndFechaRecepcionAfterAndIdLoteNot(
                        lotePago.getRucEmpresa(),
                        lotePago.getNombreArchivo(),
                        lotePago.getHashArchivo(),
                        fechaLimite,
                        lotePago.getIdLote()
                );
        if (Boolean.TRUE.equals(existeDuplicado)) {
            errores.add(new ErrorGlobalResponse("LOTE_DUPLICADO", "Existe otro lote con el mismo RUC, archivo y hash en la ventana configurada."));
        }
        TotalesValidacionResponse totales = construirTotalesValidacion(lotePago);
        if (!totales.totalRegistrosDeclarado().equals(totales.totalLineasParseadas().intValue())) {
            errores.add(new ErrorGlobalResponse("TOTAL_REGISTROS_DECLARADO_INVALIDO", "El total declarado no coincide con las lineas parseadas."));
        }
        if (!totales.totalRegistrosPie().equals(totales.totalLineasParseadas().intValue())) {
            errores.add(new ErrorGlobalResponse("TOTAL_REGISTROS_PIE_INVALIDO", "El total del pie no coincide con las lineas parseadas."));
        }
        if (totales.montoTotalDeclarado().compareTo(totales.montoTotalDetalle()) != 0) {
            errores.add(new ErrorGlobalResponse("MONTO_DECLARADO_INVALIDO", "El monto declarado no coincide con las lineas parseadas."));
        }
        if (totales.montoTotalPie().compareTo(totales.montoTotalDetalle()) != 0) {
            errores.add(new ErrorGlobalResponse("MONTO_PIE_INVALIDO", "El monto del pie no coincide con las lineas parseadas."));
        }
        return errores;
    }

    private TotalesValidacionResponse construirTotalesValidacion(LotePago lotePago) {
        Long totalLineas = lineaPagoService.contarLineas(lotePago);
        BigDecimal montoTotalDetalle = lineaPagoService.sumarMontoLineas(lotePago);
        return new TotalesValidacionResponse(
                lotePago.getTotalRegistrosDeclarado(),
                lotePago.getTotalRegistrosPie(),
                totalLineas,
                lotePago.getMontoTotalDeclarado(),
                lotePago.getMontoTotalPie(),
                montoTotalDetalle
        );
    }

    private void cambiarEstado(LotePago lotePago, EstadoLote estadoNuevo, String motivo, String cambiadoPor) {
        EstadoLote estadoAnterior = lotePago.getEstado();
        lotePago.setEstado(estadoNuevo);
        lotePago.setFechaActualizacion(OffsetDateTime.now(ZONA_HORARIA_OPERATIVA));
        lotePagoRepository.save(lotePago);
        registrarHistorialEstado(lotePago, estadoAnterior, estadoNuevo, motivo, cambiadoPor);
    }

    private void registrarHistorialEstado(LotePago lotePago, EstadoLote estadoAnterior, EstadoLote estadoNuevo,
                                          String motivo, String cambiadoPor) {
        HistorialEstadoLote historialEstadoLote = new HistorialEstadoLote();
        historialEstadoLote.setLotePago(lotePago);
        historialEstadoLote.setEstadoAnterior(estadoAnterior);
        historialEstadoLote.setEstadoNuevo(estadoNuevo);
        historialEstadoLote.setMotivo(motivo);
        historialEstadoLote.setCambiadoPor(cambiadoPor);
        historialEstadoLote.setFechaCambio(OffsetDateTime.now(ZONA_HORARIA_OPERATIVA));
        historialEstadoLoteRepository.save(historialEstadoLote);
    }

    private LotePago obtenerLotePorUuid(UUID uuidLote) {
        if (uuidLote == null) {
            throw new SolicitudInvalidaException("UUID_LOTE_REQUERIDO", "El uuidLote es obligatorio.");
        }
        return lotePagoRepository.findByUuidLote(uuidLote)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "LOTE_NO_ENCONTRADO",
                        "No existe un lote con el uuid indicado."
                ));
    }

    private Boolean esEstadoAnulable(EstadoLote estado) {
        return EstadoLote.RECIBIDO.equals(estado)
                || EstadoLote.VALIDANDO.equals(estado)
                || EstadoLote.VALIDADO.equals(estado)
                || EstadoLote.ENCOLADO.equals(estado)
                || EstadoLote.RECHAZADO.equals(estado);
    }

    private FormatoArchivo obtenerFormatoArchivo(String nombreArchivo) {
        String nombreNormalizado = nombreArchivo.toLowerCase();
        if (nombreNormalizado.endsWith(".csv")) {
            return FormatoArchivo.CSV;
        }
        if (nombreNormalizado.endsWith(".txt")) {
            return FormatoArchivo.TXT;
        }
        throw new ReglaNegocioException("FORMATO_ARCHIVO_INVALIDO", "El formato del archivo no es valido.");
    }

    private ConsultaLoteResponse construirConsultaLoteResponse(LotePago lotePago) {
        return new ConsultaLoteResponse(
                lotePago.getUuidLote(),
                lotePago.getRucEmpresa(),
                lotePago.getTipoServicio() != null ? lotePago.getTipoServicio().getCodigo() : null,
                lotePago.getNombreArchivo(),
                lotePago.getCanalIngreso() != null ? lotePago.getCanalIngreso().name() : null,
                lotePago.getEstado() != null ? lotePago.getEstado().name() : null,
                lotePago.getTotalRegistrosDeclarado(),
                lotePago.getMontoTotalDeclarado(),
                lotePago.getFechaRecepcion()
        );
    }

    private EstadoLoteResponse construirEstadoLoteResponse(LotePago lotePago) {
        return new EstadoLoteResponse(
                lotePago.getUuidLote(),
                lotePago.getEstado().name(),
                lotePago.getMotivoRechazoGlobal(),
                new ResumenEstadoLoteResponse(
                        lineaPagoService.contarLineas(lotePago),
                        lineaPagoService.contarLineasPorEstado(lotePago, EstadoLineaPago.PENDIENTE),
                        lineaPagoService.contarLineasPorEstado(lotePago, EstadoLineaPago.VALIDADA),
                        lineaPagoService.contarLineasPorEstado(lotePago, EstadoLineaPago.RECHAZADA)
                ),
                new FechasLoteResponse(
                        lotePago.getFechaRecepcion(),
                        lotePago.getFechaInicioValidacion(),
                        lotePago.getFechaFinValidacion(),
                        lotePago.getFechaInicioProceso(),
                        lotePago.getFechaFinProceso(),
                        lotePago.getFechaCierre()
                ),
                obtenerAccionesDisponibles(lotePago)
        );
    }

    private List<String> obtenerAccionesDisponibles(LotePago lotePago) {
        List<String> acciones = new ArrayList<>();
        if (EstadoLote.RECIBIDO.equals(lotePago.getEstado()) || EstadoLote.ENCOLADO.equals(lotePago.getEstado())) {
            acciones.add("VALIDAR");
        }
        if (esEstadoAnulable(lotePago.getEstado())) {
            acciones.add("ANULAR");
        }
        return acciones;
    }

    private LineaPagoResponse construirLineaPagoResponse(LineaPagoInternalDto lineaPagoInternalDto) {
        return new LineaPagoResponse(
                lineaPagoInternalDto.uuidOperacionSwitch(),
                lineaPagoInternalDto.secuencial(),
                lineaPagoInternalDto.identificacionBeneficiario(),
                lineaPagoInternalDto.nombreBeneficiario(),
                lineaPagoInternalDto.cuentaDestino(),
                lineaPagoInternalDto.monto(),
                lineaPagoInternalDto.conceptoReferencia(),
                lineaPagoInternalDto.correoNotificacion(),
                lineaPagoInternalDto.estado(),
                lineaPagoInternalDto.codigoError(),
                lineaPagoInternalDto.mensajeError(),
                lineaPagoInternalDto.fechaValidacion()
        );
    }

    private LoteProcesamientoInternalDto construirLoteProcesamientoInternalDto(LotePago lotePago) {
        return new LoteProcesamientoInternalDto(
                lotePago.getIdLote(),
                lotePago.getUuidLote(),
                lotePago.getRucEmpresa(),
                lotePago.getTipoServicio() != null ? lotePago.getTipoServicio().getCodigo() : null,
                lotePago.getCuentaMatrizCargo(),
                lotePago.getEstado(),
                lotePago.getFechaInicioProceso(),
                lotePago.getFechaFinProceso()
        );
    }

    private <T> PaginaResponse<T> construirPaginaResponse(Page<T> pagina) {
        return new PaginaResponse<>(
                pagina.getContent(),
                pagina.getNumber(),
                pagina.getSize(),
                pagina.getTotalElements(),
                pagina.getTotalPages()
        );
    }

    private void registrarAuditoria(LotePago lotePago, String accion, ObjectNode datosAntes, ObjectNode datosDespues) {
        RegistroAuditoriaRequest registroAuditoriaRequest = new RegistroAuditoriaRequest();
        registroAuditoriaRequest.setTipoActor(TipoActorAuditoria.SISTEMA);
        registroAuditoriaRequest.setIdActor("SWITCH");
        registroAuditoriaRequest.setRucEmpresa(lotePago.getRucEmpresa());
        registroAuditoriaRequest.setAccion(accion);
        registroAuditoriaRequest.setEntidad("LOTE_PAGO");
        registroAuditoriaRequest.setIdEntidad(lotePago.getUuidLote().toString());
        registroAuditoriaRequest.setDatosAntes(datosAntes);
        registroAuditoriaRequest.setDatosDespues(datosDespues);
        auditoriaSwitchService.registrarAccion(registroAuditoriaRequest);
    }

    private ObjectNode construirDatosBasicos(LotePago lotePago) {
        ObjectNode datos = objectMapper.createObjectNode();
        datos.put("uuidLote", lotePago.getUuidLote().toString());
        datos.put("estado", lotePago.getEstado().name());
        datos.put("rucEmpresa", lotePago.getRucEmpresa());
        datos.put("nombreArchivo", lotePago.getNombreArchivo());
        datos.put("hashArchivo", lotePago.getHashArchivo());
        return datos;
    }

    private ObjectNode construirEstadoAuditoria(EstadoLote estado) {
        ObjectNode datos = objectMapper.createObjectNode();
        datos.put("estado", estado.name());
        return datos;
    }
}
