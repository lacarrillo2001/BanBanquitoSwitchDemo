package com.banquito.switchpagos.batch.service.impl;

import com.banquito.switchpagos.file.dto.internal.ArchivoPagoParseadoInternalDto;
import com.banquito.switchpagos.file.dto.internal.ErrorValidacionArchivoInternalDto;
import com.banquito.switchpagos.file.dto.internal.ResultadoValidacionArchivoInternalDto;
import com.banquito.switchpagos.file.service.ArchivoPagoService;
import com.banquito.switchpagos.file.service.ValidadorArchivoPagoService;
import com.banquito.switchpagos.audit.dto.internal.RegistroAuditoriaRequest;
import com.banquito.switchpagos.audit.enums.TipoActorAuditoria;
import com.banquito.switchpagos.audit.service.AuditoriaSwitchService;
import com.banquito.switchpagos.catalog.model.TipoServicio;
import com.banquito.switchpagos.catalog.service.TipoServicioService;
import com.banquito.switchpagos.shared.exception.ConflictoOperacionException;
import com.banquito.switchpagos.shared.exception.EstadoInvalidoException;
import com.banquito.switchpagos.shared.exception.RecursoNoEncontradoException;
import com.banquito.switchpagos.shared.exception.ReglaNegocioException;
import com.banquito.switchpagos.shared.exception.SolicitudInvalidaException;
import com.banquito.switchpagos.batch.dto.api.AnulacionLoteResponse;
import com.banquito.switchpagos.batch.dto.api.CargaLoteResponse;
import com.banquito.switchpagos.batch.dto.api.ConsultaLoteResponse;
import com.banquito.switchpagos.batch.dto.api.ErrorGlobalResponse;
import com.banquito.switchpagos.batch.dto.api.EstadoLoteResponse;
import com.banquito.switchpagos.batch.dto.api.LineaPagoResponse;
import com.banquito.switchpagos.batch.dto.api.PaginaResponse;
import com.banquito.switchpagos.batch.dto.api.ResumenEstadoLoteResponse;
import com.banquito.switchpagos.batch.dto.api.TotalesValidacionResponse;
import com.banquito.switchpagos.batch.dto.api.ValidacionLoteResponse;
import com.banquito.switchpagos.batch.dto.internal.LoteProcesamientoInternalDto;
import com.banquito.switchpagos.batch.dto.internal.RegistroLoteInternalDto;
import com.banquito.switchpagos.batch.enums.CanalIngreso;
import com.banquito.switchpagos.batch.enums.EstadoLote;
import com.banquito.switchpagos.batch.enums.FormatoArchivo;
import com.banquito.switchpagos.batch.model.HistorialEstadoLote;
import com.banquito.switchpagos.batch.model.LotePago;
import com.banquito.switchpagos.batch.mapper.ColaProcesamientoMapper;
import com.banquito.switchpagos.batch.mapper.HistorialEstadoLoteMapper;
import com.banquito.switchpagos.batch.mapper.LotePagoMapper;
import com.banquito.switchpagos.batch.repository.ColaProcesamientoRepository;
import com.banquito.switchpagos.batch.repository.HistorialEstadoLoteRepository;
import com.banquito.switchpagos.batch.repository.LotePagoRepository;
import com.banquito.switchpagos.batch.service.LotePagoService;
import com.banquito.switchpagos.integrationcore.dto.internal.DiaHabilCoreResponse;
import com.banquito.switchpagos.integrationcore.dto.internal.CuentaFavoritaPagosCoreResponse;
import com.banquito.switchpagos.integrationcore.dto.internal.ValidacionCoreResponse;
import com.banquito.switchpagos.integrationcore.service.CoreBancarioService;
import com.banquito.switchpagos.parameter.constants.CodigoParametroSwitch;
import com.banquito.switchpagos.parameter.service.ParametroSwitchService;
import com.banquito.switchpagos.processing.enums.EstadoLineaPago;
import com.banquito.switchpagos.processing.mapper.LineaPagoMapper;
import com.banquito.switchpagos.processing.service.LineaPagoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
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
    private final CoreBancarioService coreBancarioService;
    private final AuditoriaSwitchService auditoriaSwitchService;
    private final ObjectMapper objectMapper;
    private final LotePagoMapper lotePagoMapper;
    private final HistorialEstadoLoteMapper historialEstadoLoteMapper;
    private final ColaProcesamientoMapper colaProcesamientoMapper;
    private final LineaPagoMapper lineaPagoMapper;

    public LotePagoServiceImpl(LotePagoRepository lotePagoRepository,
                               HistorialEstadoLoteRepository historialEstadoLoteRepository,
                               ColaProcesamientoRepository colaProcesamientoRepository,
                               ArchivoPagoService archivoPagoService,
                               ValidadorArchivoPagoService validadorArchivoPagoService,
                               ParametroSwitchService parametroSwitchService,
                               TipoServicioService tipoServicioService,
                               LineaPagoService lineaPagoService,
                               CoreBancarioService coreBancarioService,
                               AuditoriaSwitchService auditoriaSwitchService,
                               ObjectMapper objectMapper,
                               LotePagoMapper lotePagoMapper,
                               HistorialEstadoLoteMapper historialEstadoLoteMapper,
                               ColaProcesamientoMapper colaProcesamientoMapper,
                               LineaPagoMapper lineaPagoMapper) {
        this.lotePagoRepository = lotePagoRepository;
        this.historialEstadoLoteRepository = historialEstadoLoteRepository;
        this.colaProcesamientoRepository = colaProcesamientoRepository;
        this.archivoPagoService = archivoPagoService;
        this.validadorArchivoPagoService = validadorArchivoPagoService;
        this.parametroSwitchService = parametroSwitchService;
        this.tipoServicioService = tipoServicioService;
        this.lineaPagoService = lineaPagoService;
        this.coreBancarioService = coreBancarioService;
        this.auditoriaSwitchService = auditoriaSwitchService;
        this.objectMapper = objectMapper;
        this.lotePagoMapper = lotePagoMapper;
        this.historialEstadoLoteMapper = historialEstadoLoteMapper;
        this.colaProcesamientoMapper = colaProcesamientoMapper;
        this.lineaPagoMapper = lineaPagoMapper;
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
        RegistroLoteInternalDto registroNormalizado = resolverCuentaMatrizSegunCanal(
                registroLoteInternalDto,
                archivoPagoParseado
        );
        validarDatosSolicitud(registroNormalizado, archivoPagoParseado);
        validarCredencialSolicitud(registroNormalizado, archivoPagoParseado);
        validarDuplicidad(archivoPagoParseado.cabecera().rucEmpresa(), archivoPagoParseado.nombreArchivo(),
                archivoPagoParseado.hashArchivo());

        OffsetDateTime fechaRecepcion = OffsetDateTime.now(ZONA_HORARIA_OPERATIVA);
        EstadoLote estadoInicial = calcularEstadoInicial(fechaRecepcion);
        LotePago lotePago = construirLotePago(registroNormalizado, archivoPagoParseado, estadoInicial, fechaRecepcion);
        lotePagoRepository.save(lotePago);
        lineaPagoService.guardarLineasPendientes(lotePago, archivoPagoParseado.detalles());
        registrarHistorialEstado(lotePago, null, estadoInicial, "Registro inicial del lote.", "SISTEMA");
        if (EstadoLote.ENCOLADO.equals(estadoInicial)) {
            registrarColaProcesamiento(lotePago, fechaRecepcion);
        }
        registrarAuditoria(lotePago, "CREACION_LOTE", null, construirDatosBasicos(lotePago));
        return lotePagoMapper.toCargaResponse(lotePago);
    }

    @Override
    @Transactional(readOnly = true)
    public PaginaResponse<ConsultaLoteResponse> consultarLotes(String rucEmpresa, EstadoLote estado, String tipoServicio,
                                                               OffsetDateTime fechaDesde, OffsetDateTime fechaHasta,
                                                               Pageable pageable) {
        Page<ConsultaLoteResponse> pagina = lotePagoRepository.findAll(
                construirFiltrosConsulta(rucEmpresa, estado, tipoServicio, fechaDesde, fechaHasta),
                pageable
        ).map(lotePagoMapper::toConsultaResponse);
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
        return lotePagoMapper.toAnulacionResponse(lotePago, motivo);
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
            return lotePagoMapper.toValidacionResponse(lotePago, Boolean.TRUE, totales, errores);
        }

        String motivoRechazo = errores.getFirst().mensaje();
        lotePago.setMotivoRechazoGlobal(motivoRechazo);
        lotePago.setFechaFinValidacion(OffsetDateTime.now(ZONA_HORARIA_OPERATIVA));
        cambiarEstado(lotePago, EstadoLote.RECHAZADO, motivoRechazo, "SISTEMA");
        registrarAuditoria(lotePago, "VALIDACION_LOTE_RECHAZADA", construirEstadoAuditoria(estadoAnterior),
                construirDatosBasicos(lotePago));
        return lotePagoMapper.toValidacionResponse(lotePago, Boolean.FALSE, totales, errores);
    }

    @Override
    @Transactional(readOnly = true)
    public PaginaResponse<LineaPagoResponse> consultarLineas(UUID uuidLote, EstadoLineaPago estado, Pageable pageable) {
        LotePago lotePago = obtenerLotePorUuid(uuidLote);
        Page<LineaPagoResponse> pagina = lineaPagoService.consultarLineas(lotePago, estado, pageable)
                .map(lineaPagoMapper::toResponse);
        return construirPaginaResponse(pagina);
    }

    @Override
    @Transactional(readOnly = true)
    public LoteProcesamientoInternalDto obtenerDatosProcesamiento(UUID uuidLote) {
        LotePago lotePago = obtenerLotePorUuid(uuidLote);
        return lotePagoMapper.toProcesamientoInternalDto(lotePago);
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
        if (!CanalIngreso.SFTP.equals(registroLoteInternalDto.canalIngreso())
                && !archivoPagoParseado.cabecera().cuentaMatrizCargo().equals(registroLoteInternalDto.cuentaMatrizCargo())) {
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

    private RegistroLoteInternalDto resolverCuentaMatrizSegunCanal(RegistroLoteInternalDto registroLoteInternalDto,
                                                                   ArchivoPagoParseadoInternalDto archivoPagoParseado) {
        if (!CanalIngreso.SFTP.equals(registroLoteInternalDto.canalIngreso())) {
            return registroLoteInternalDto;
        }

        CuentaFavoritaPagosCoreResponse cuentaFavorita = coreBancarioService.obtenerCuentaFavoritaPagos(
                archivoPagoParseado.cabecera().rucEmpresa()
        );
        if (!Boolean.TRUE.equals(cuentaFavorita.valida()) || cuentaFavorita.numeroCuenta() == null
                || cuentaFavorita.numeroCuenta().isBlank()) {
            throw new ReglaNegocioException(
                    codigoError(cuentaFavorita.codigo(), "CUENTA_FAVORITA_INVALIDA"),
                    mensajeError(cuentaFavorita.mensaje(), "La empresa no tiene una cuenta favorita valida para pagos masivos.")
            );
        }

        return new RegistroLoteInternalDto(
                registroLoteInternalDto.archivo(),
                registroLoteInternalDto.tipoServicio(),
                cuentaFavorita.numeroCuenta(),
                registroLoteInternalDto.canalIngreso(),
                registroLoteInternalDto.idCredencialWebCore(),
                registroLoteInternalDto.usernameCredencialWebCore(),
                registroLoteInternalDto.rucEmpresa()
        );
    }

    private Specification<LotePago> construirFiltrosConsulta(String rucEmpresa,
                                                             EstadoLote estado,
                                                             String tipoServicio,
                                                             OffsetDateTime fechaDesde,
                                                             OffsetDateTime fechaHasta) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (rucEmpresa != null && !rucEmpresa.isBlank()) {
                predicates.add(criteriaBuilder.equal(root.get("rucEmpresa"), rucEmpresa));
            }
            if (estado != null) {
                predicates.add(criteriaBuilder.equal(root.get("estado"), estado));
            }
            if (tipoServicio != null && !tipoServicio.isBlank()) {
                predicates.add(criteriaBuilder.equal(root.get("tipoServicio").get("codigo"), tipoServicio));
            }
            if (fechaDesde != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("fechaRecepcion"), fechaDesde));
            }
            if (fechaHasta != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("fechaRecepcion"), fechaHasta));
            }
            return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
        };
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
        TipoServicio tipoServicio = tipoServicioService.obtenerEntidadActiva(archivoPagoParseado.cabecera().tipoServicio());
        return lotePagoMapper.toEntity(
                registroLoteInternalDto,
                archivoPagoParseado,
                tipoServicio,
                obtenerFormatoArchivo(archivoPagoParseado.nombreArchivo()),
                estadoInicial,
                fechaRecepcion
        );
    }

    private EstadoLote calcularEstadoInicial(OffsetDateTime fechaRecepcion) {
        LocalTime horaCorte = parametroSwitchService.obtenerHora(CodigoParametroSwitch.HORA_CORTE_PROCESO);
        LocalDate fechaLocal = fechaRecepcion.toLocalDate();
        DiaHabilCoreResponse diaHabil = coreBancarioService.consultarDiaHabil(fechaLocal);
        if (Boolean.TRUE.equals(diaHabil.esDiaHabil()) && fechaRecepcion.toLocalTime().isBefore(horaCorte)) {
            return EstadoLote.RECIBIDO;
        }
        return EstadoLote.ENCOLADO;
    }

    private void registrarColaProcesamiento(LotePago lotePago, OffsetDateTime fechaRecepcion) {
        LocalTime horaInicio = parametroSwitchService.obtenerHora(CodigoParametroSwitch.HORA_INICIO_LOTES_ENCOLADOS);
        LocalDate siguienteDiaHabil = obtenerSiguienteDiaHabil(fechaRecepcion.toLocalDate());
        colaProcesamientoRepository.save(colaProcesamientoMapper.toEntity(
                lotePago,
                siguienteDiaHabil,
                fechaRecepcion,
                siguienteDiaHabil.atTime(horaInicio).atZone(ZONA_HORARIA_OPERATIVA).toOffsetDateTime(),
                parametroSwitchService.obtenerInteger(CodigoParametroSwitch.MAX_REINTENTOS_LOTE)
        ));
    }

    private LocalDate obtenerSiguienteDiaHabil(LocalDate fechaBase) {
        DiaHabilCoreResponse diaHabil = coreBancarioService.consultarDiaHabil(fechaBase);
        return diaHabil.siguienteDiaHabil();
    }

    private List<ErrorGlobalResponse> validarReglasLote(LotePago lotePago) {
        List<ErrorGlobalResponse> errores = new ArrayList<>();
        validarReglasCore(lotePago, errores);
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

    private void validarReglasCore(LotePago lotePago, List<ErrorGlobalResponse> errores) {
        ValidacionCoreResponse validacionEmpresa = coreBancarioService.validarEmpresa(lotePago.getRucEmpresa());
        if (!Boolean.TRUE.equals(validacionEmpresa.valida())) {
            errores.add(new ErrorGlobalResponse(
                    codigoError(validacionEmpresa, "EMPRESA_NO_HABILITADA"),
                    mensajeError(validacionEmpresa, "La empresa emisora no esta habilitada en Core para pagos masivos.")
            ));
            return;
        }

        if (CanalIngreso.SFTP.equals(lotePago.getCanalIngreso())) {
            validarCuentaFavoritaSftp(lotePago, errores);
            return;
        }

        ValidacionCoreResponse validacionCuentaMatriz = coreBancarioService.validarCuentaMatriz(
                lotePago.getRucEmpresa(),
                lotePago.getCuentaMatrizCargo()
        );
        if (!Boolean.TRUE.equals(validacionCuentaMatriz.valida())) {
            errores.add(new ErrorGlobalResponse(
                    codigoError(validacionCuentaMatriz, "CUENTA_MATRIZ_INVALIDA"),
                    mensajeError(validacionCuentaMatriz, "La cuenta matriz no es valida para pagos masivos.")
            ));
        }
    }

    private void validarCuentaFavoritaSftp(LotePago lotePago, List<ErrorGlobalResponse> errores) {
        CuentaFavoritaPagosCoreResponse cuentaFavorita = coreBancarioService.obtenerCuentaFavoritaPagos(
                lotePago.getRucEmpresa()
        );
        if (!Boolean.TRUE.equals(cuentaFavorita.valida())) {
            errores.add(new ErrorGlobalResponse(
                    codigoError(cuentaFavorita.codigo(), "CUENTA_FAVORITA_INVALIDA"),
                    mensajeError(cuentaFavorita.mensaje(), "La cuenta favorita de pagos masivos no es valida.")
            ));
            return;
        }
        if (!lotePago.getCuentaMatrizCargo().equals(cuentaFavorita.numeroCuenta())) {
            errores.add(new ErrorGlobalResponse(
                    "CUENTA_FAVORITA_NO_COINCIDE",
                    "La cuenta matriz del lote SFTP no coincide con la cuenta favorita vigente en Core."
            ));
        }
    }

    private void validarCredencialSolicitud(RegistroLoteInternalDto registroLoteInternalDto,
                                            ArchivoPagoParseadoInternalDto archivoPagoParseado) {
        if (registroLoteInternalDto.usernameCredencialWebCore() == null
                || registroLoteInternalDto.usernameCredencialWebCore().isBlank()) {
            return;
        }
        ValidacionCoreResponse validacionCredencial = coreBancarioService.validarCredencialEmpresa(
                archivoPagoParseado.cabecera().rucEmpresa(),
                registroLoteInternalDto.usernameCredencialWebCore()
        );
        if (!Boolean.TRUE.equals(validacionCredencial.valida())) {
            throw new ReglaNegocioException(
                    codigoError(validacionCredencial, "CREDENCIAL_EMPRESARIAL_INVALIDA"),
                    mensajeError(validacionCredencial, "La credencial empresarial no es valida para pagos masivos.")
            );
        }
    }

    private String codigoError(ValidacionCoreResponse validacionCoreResponse, String codigoDefault) {
        if (validacionCoreResponse.codigo() == null || validacionCoreResponse.codigo().isBlank()) {
            return codigoDefault;
        }
        return validacionCoreResponse.codigo();
    }

    private String codigoError(String codigo, String codigoDefault) {
        if (codigo == null || codigo.isBlank()) {
            return codigoDefault;
        }
        return codigo;
    }

    private String mensajeError(ValidacionCoreResponse validacionCoreResponse, String mensajeDefault) {
        if (validacionCoreResponse.mensaje() == null || validacionCoreResponse.mensaje().isBlank()) {
            return mensajeDefault;
        }
        return validacionCoreResponse.mensaje();
    }

    private String mensajeError(String mensaje, String mensajeDefault) {
        if (mensaje == null || mensaje.isBlank()) {
            return mensajeDefault;
        }
        return mensaje;
    }

    private TotalesValidacionResponse construirTotalesValidacion(LotePago lotePago) {
        Long totalLineas = lineaPagoService.contarLineas(lotePago);
        BigDecimal montoTotalDetalle = lineaPagoService.sumarMontoLineas(lotePago);
        return lotePagoMapper.toTotalesValidacionResponse(lotePago, totalLineas, montoTotalDetalle);
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
        HistorialEstadoLote historialEstadoLote = historialEstadoLoteMapper.toEntity(
                lotePago,
                estadoAnterior,
                estadoNuevo,
                motivo,
                cambiadoPor,
                OffsetDateTime.now(ZONA_HORARIA_OPERATIVA)
        );
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

    private EstadoLoteResponse construirEstadoLoteResponse(LotePago lotePago) {
        ResumenEstadoLoteResponse resumen = new ResumenEstadoLoteResponse(
                lineaPagoService.contarLineas(lotePago),
                lineaPagoService.contarLineasPorEstado(lotePago, EstadoLineaPago.PENDIENTE),
                lineaPagoService.contarLineasPorEstado(lotePago, EstadoLineaPago.VALIDADA),
                lineaPagoService.contarLineasPorEstado(lotePago, EstadoLineaPago.RECHAZADA)
        );
        return lotePagoMapper.toEstadoResponse(lotePago, resumen, obtenerAccionesDisponibles(lotePago));
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
        return lotePagoMapper.toDatosBasicosNode(lotePago, objectMapper);
    }

    private ObjectNode construirEstadoAuditoria(EstadoLote estado) {
        ObjectNode datos = objectMapper.createObjectNode();
        datos.put("estado", estado.name());
        return datos;
    }
}
