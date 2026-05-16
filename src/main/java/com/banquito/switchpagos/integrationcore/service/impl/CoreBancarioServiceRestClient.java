package com.banquito.switchpagos.integrationcore.service.impl;

import com.banquito.switchpagos.audit.dto.internal.RegistroAuditoriaRequest;
import com.banquito.switchpagos.audit.enums.TipoActorAuditoria;
import com.banquito.switchpagos.audit.service.AuditoriaSwitchService;
import com.banquito.switchpagos.integrationcore.config.CoreBancarioProperties;
import com.banquito.switchpagos.integrationcore.dto.internal.ApiResponseCore;
import com.banquito.switchpagos.integrationcore.dto.internal.AutenticacionCoreRequest;
import com.banquito.switchpagos.integrationcore.dto.internal.AutenticacionCoreResponse;
import com.banquito.switchpagos.integrationcore.dto.internal.ConsultaSaldoCoreResponse;
import com.banquito.switchpagos.integrationcore.dto.internal.CuentaFavoritaPagosCoreResponse;
import com.banquito.switchpagos.integrationcore.dto.internal.DiaHabilCoreResponse;
import com.banquito.switchpagos.integrationcore.dto.internal.ErrorCoreResponse;
import com.banquito.switchpagos.integrationcore.dto.internal.LiquidacionCoreApiResponse;
import com.banquito.switchpagos.integrationcore.dto.internal.LiquidacionCoreRequest;
import com.banquito.switchpagos.integrationcore.dto.internal.LiquidacionCoreResponse;
import com.banquito.switchpagos.integrationcore.dto.internal.MovimientoCoreRequest;
import com.banquito.switchpagos.integrationcore.dto.internal.MovimientoCoreResponse;
import com.banquito.switchpagos.integrationcore.dto.internal.SaldoCuentaCoreApiResponse;
import com.banquito.switchpagos.integrationcore.dto.internal.TransferenciaCoreRequest;
import com.banquito.switchpagos.integrationcore.dto.internal.TransferenciaCoreResponse;
import com.banquito.switchpagos.integrationcore.dto.internal.ValidacionCoreResponse;
import com.banquito.switchpagos.integrationcore.dto.internal.ValidacionCredencialEmpresaCoreApiResponse;
import com.banquito.switchpagos.integrationcore.dto.internal.ValidacionCuentaCoreResponse;
import com.banquito.switchpagos.integrationcore.dto.internal.ValidacionCuentaDestinoCoreApiResponse;
import com.banquito.switchpagos.integrationcore.dto.internal.ValidacionCuentaMatrizCoreApiResponse;
import com.banquito.switchpagos.integrationcore.dto.internal.ValidacionEmpresaCoreApiResponse;
import com.banquito.switchpagos.integrationcore.service.CoreBancarioService;
import com.banquito.switchpagos.shared.exception.IntegracionCoreException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.ResourceAccessException;

import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
@ConditionalOnProperty(name = "core.integration.mode", havingValue = "rest", matchIfMissing = true)
public class CoreBancarioServiceRestClient implements CoreBancarioService {

    private static final String MENSAJE_CORE_NO_DISPONIBLE = "No fue posible comunicarse con el Core Bancario.";

    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final CoreBancarioProperties properties;
    private final AuditoriaSwitchService auditoriaSwitchService;
    private final Map<UUID, TransferenciaCoreResponse> transferenciasPorOperacion;

    public CoreBancarioServiceRestClient(RestClient coreBancarioRestClient,
                                         ObjectMapper objectMapper,
                                         CoreBancarioProperties properties,
                                         AuditoriaSwitchService auditoriaSwitchService) {
        this.restClient = coreBancarioRestClient;
        this.objectMapper = objectMapper;
        this.properties = properties;
        this.auditoriaSwitchService = auditoriaSwitchService;
        this.transferenciasPorOperacion = new ConcurrentHashMap<>();
    }

    @Override
    public AutenticacionCoreResponse autenticar(String usuario, String contrasena) {
        if (Boolean.TRUE.equals(properties.getIntegration().getMockAutenticacion())) {
            AutenticacionCoreResponse autenticacion = construirAutenticacionMock(usuario, contrasena);
            registrarAuditoria("CORE_LOGIN_SWITCH_MOCK", usuario, autenticacion.rolSwitch());
            return autenticacion;
        }
        try {
            ApiResponseCore<AutenticacionCoreResponse> response = restClient.post()
                    .uri("/api/v1/core/integracion-switch/autenticacion/login")
                    .body(new AutenticacionCoreRequest(usuario, contrasena))
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {
                    });

            AutenticacionCoreResponse autenticacion = obtenerData(response);
            registrarAuditoria("CORE_LOGIN_SWITCH", usuario, autenticacion.rolSwitch());
            return autenticacion;
        } catch (RestClientResponseException exception) {
            ErrorCoreResponse error = leerErrorCore(exception);
            String codigo = mapearCodigoCore(error.code(), exception.getStatusCode());
            registrarAuditoria("CORE_LOGIN_SWITCH_RECHAZADO", usuario, codigo);
            return new AutenticacionCoreResponse(
                    Boolean.FALSE,
                    null,
                    null,
                    null,
                    null,
                    usuario,
                    null,
                    null,
                    null,
                    Boolean.FALSE
            );
        } catch (ResourceAccessException exception) {
            registrarAuditoria("CORE_LOGIN_SWITCH_ERROR_TECNICO", usuario, "CORE_NO_DISPONIBLE");
            throw new IntegracionCoreException("CORE_NO_DISPONIBLE", MENSAJE_CORE_NO_DISPONIBLE, exception);
        }
    }

    @Override
    public ValidacionCoreResponse validarEmpresa(String ruc) {
        try {
            ApiResponseCore<ValidacionEmpresaCoreApiResponse> response = restClient.get()
                    .uri("/api/v1/core/integracion-switch/empresas/{ruc}/validacion", ruc)
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {
                    });

            ValidacionEmpresaCoreApiResponse validacion = obtenerData(response);
            registrarAuditoria("CORE_VALIDACION_EMPRESA", ruc, validacion.codigo());
            return new ValidacionCoreResponse(
                    Boolean.TRUE.equals(validacion.habilitada()),
                    mapearCodigoValidacionEmpresa(validacion.codigo()),
                    validacion.mensaje()
            );
        } catch (RestClientResponseException exception) {
            ErrorCoreResponse error = leerErrorCore(exception);
            String codigo = esNoEncontrado(error.code(), exception.getStatusCode())
                    ? "EMPRESA_NO_EXISTE"
                    : mapearCodigoValidacionEmpresa(error.code());
            registrarAuditoria("CORE_VALIDACION_EMPRESA_RECHAZADA", ruc, codigo);
            return new ValidacionCoreResponse(
                    Boolean.FALSE,
                    codigo,
                    mensajeCore(error.message(), "No fue posible validar la empresa en Core.")
            );
        } catch (ResourceAccessException exception) {
            registrarAuditoria("CORE_VALIDACION_EMPRESA_ERROR_TECNICO", ruc, "CORE_NO_DISPONIBLE");
            throw new IntegracionCoreException("CORE_NO_DISPONIBLE", MENSAJE_CORE_NO_DISPONIBLE, exception);
        }
    }

    @Override
    public ValidacionCoreResponse validarCuentaMatriz(String ruc, String numeroCuenta) {
        try {
            ApiResponseCore<ValidacionCuentaMatrizCoreApiResponse> response = restClient.get()
                    .uri("/api/v1/core/integracion-switch/empresas/{ruc}/cuentas/{numeroCuenta}/validacion-matriz",
                            ruc,
                            numeroCuenta)
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {
                    });

            ValidacionCuentaMatrizCoreApiResponse validacion = obtenerData(response);
            registrarAuditoria("CORE_VALIDACION_CUENTA_MATRIZ", numeroCuenta, validacion.codigo());
            return new ValidacionCoreResponse(
                    Boolean.TRUE.equals(validacion.valida()),
                    mapearCodigoValidacionMatriz(validacion.codigo()),
                    validacion.mensaje()
            );
        } catch (RestClientResponseException exception) {
            ErrorCoreResponse error = leerErrorCore(exception);
            String codigo = esNoEncontrado(error.code(), exception.getStatusCode())
                    ? "CUENTA_MATRIZ_NO_EXISTE"
                    : mapearCodigoValidacionMatriz(error.code());
            registrarAuditoria("CORE_VALIDACION_CUENTA_MATRIZ_RECHAZADA", numeroCuenta, codigo);
            return new ValidacionCoreResponse(
                    Boolean.FALSE,
                    codigo,
                    mensajeCore(error.message(), "No fue posible validar la cuenta matriz en Core.")
            );
        } catch (ResourceAccessException exception) {
            registrarAuditoria("CORE_VALIDACION_CUENTA_MATRIZ_ERROR_TECNICO", numeroCuenta, "CORE_NO_DISPONIBLE");
            throw new IntegracionCoreException("CORE_NO_DISPONIBLE", MENSAJE_CORE_NO_DISPONIBLE, exception);
        }
    }

    @Override
    public CuentaFavoritaPagosCoreResponse obtenerCuentaFavoritaPagos(String ruc) {
        if (Boolean.TRUE.equals(properties.getIntegration().getMockCuentaFavoritaPagos())) {
            CuentaFavoritaPagosCoreResponse cuentaFavorita = construirCuentaFavoritaMock(ruc);
            registrarAuditoria("CORE_CUENTA_FAVORITA_PAGOS_MOCK", ruc, cuentaFavorita.codigo());
            return cuentaFavorita;
        }
        try {
            ApiResponseCore<CuentaFavoritaPagosCoreResponse> response = restClient.get()
                    .uri("/api/v1/core/integracion-switch/empresas/{ruc}/cuenta-favorita-pagos", ruc)
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {
                    });

            CuentaFavoritaPagosCoreResponse cuentaFavorita = obtenerData(response);
            registrarAuditoria("CORE_CUENTA_FAVORITA_PAGOS", ruc, cuentaFavorita.codigo());
            return cuentaFavorita;
        } catch (RestClientResponseException exception) {
            ErrorCoreResponse error = leerErrorCore(exception);
            String codigo = esNoEncontrado(error.code(), exception.getStatusCode())
                    ? "CUENTA_FAVORITA_NO_EXISTE"
                    : mapearCodigoCuentaFavorita(error.code());
            registrarAuditoria("CORE_CUENTA_FAVORITA_PAGOS_RECHAZADA", ruc, codigo);
            return new CuentaFavoritaPagosCoreResponse(
                    ruc,
                    Boolean.FALSE,
                    null,
                    null,
                    Boolean.FALSE,
                    null,
                    Boolean.FALSE,
                    Boolean.FALSE,
                    codigo,
                    mensajeCore(error.message(), "No fue posible obtener la cuenta favorita de pagos en Core.")
            );
        } catch (ResourceAccessException exception) {
            registrarAuditoria("CORE_CUENTA_FAVORITA_PAGOS_ERROR_TECNICO", ruc, "CORE_NO_DISPONIBLE");
            throw new IntegracionCoreException("CORE_NO_DISPONIBLE", MENSAJE_CORE_NO_DISPONIBLE, exception);
        }
    }

    @Override
    public ValidacionCoreResponse validarCredencialEmpresa(String ruc, String username) {
        try {
            ApiResponseCore<ValidacionCredencialEmpresaCoreApiResponse> response = restClient.get()
                    .uri("/api/v1/core/integracion-switch/empresas/{ruc}/credenciales/{username}/validacion",
                            ruc,
                            username)
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {
                    });

            ValidacionCredencialEmpresaCoreApiResponse validacion = obtenerData(response);
            registrarAuditoria("CORE_VALIDACION_CREDENCIAL_EMPRESA", username, validacion.codigo());
            return new ValidacionCoreResponse(
                    Boolean.TRUE.equals(validacion.valida()),
                    mapearCodigoValidacionCredencial(validacion.codigo()),
                    validacion.mensaje()
            );
        } catch (RestClientResponseException exception) {
            ErrorCoreResponse error = leerErrorCore(exception);
            String codigo = esNoEncontrado(error.code(), exception.getStatusCode())
                    ? "CREDENCIAL_EMPRESARIAL_NO_EXISTE"
                    : mapearCodigoValidacionCredencial(error.code());
            registrarAuditoria("CORE_VALIDACION_CREDENCIAL_EMPRESA_RECHAZADA", username, codigo);
            return new ValidacionCoreResponse(
                    Boolean.FALSE,
                    codigo,
                    mensajeCore(error.message(), "No fue posible validar la credencial empresarial en Core.")
            );
        } catch (ResourceAccessException exception) {
            registrarAuditoria("CORE_VALIDACION_CREDENCIAL_EMPRESA_ERROR_TECNICO", username, "CORE_NO_DISPONIBLE");
            throw new IntegracionCoreException("CORE_NO_DISPONIBLE", MENSAJE_CORE_NO_DISPONIBLE, exception);
        }
    }

    @Override
    public DiaHabilCoreResponse consultarDiaHabil(LocalDate fecha) {
        try {
            ApiResponseCore<DiaHabilCoreResponse> response = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/api/v1/core/integracion-switch/calendario/dia-habil")
                            .queryParam("fecha", fecha)
                            .build())
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {
                    });

            DiaHabilCoreResponse diaHabil = obtenerData(response);
            registrarAuditoria("CORE_CONSULTA_DIA_HABIL", fecha.toString(), diaHabil.codigo());
            return diaHabil;
        } catch (RestClientResponseException exception) {
            ErrorCoreResponse error = leerErrorCore(exception);
            String codigo = mapearCodigoCore(error.code(), exception.getStatusCode());
            registrarAuditoria("CORE_CONSULTA_DIA_HABIL_RECHAZADA", fecha.toString(), codigo);
            throw new IntegracionCoreException(
                    codigo,
                    mensajeCore(error.message(), "No fue posible consultar el calendario operativo en Core."),
                    exception
            );
        } catch (ResourceAccessException exception) {
            registrarAuditoria("CORE_CONSULTA_DIA_HABIL_ERROR_TECNICO", fecha.toString(), "CORE_NO_DISPONIBLE");
            throw new IntegracionCoreException("CORE_NO_DISPONIBLE", MENSAJE_CORE_NO_DISPONIBLE, exception);
        }
    }

    @Override
    public ConsultaSaldoCoreResponse consultarSaldoDisponible(String numeroCuenta) {
        try {
            ApiResponseCore<SaldoCuentaCoreApiResponse> response = restClient.get()
                    .uri("/api/v1/core/integracion-switch/cuentas/{numeroCuenta}/disponibilidad", numeroCuenta)
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {
                    });

            SaldoCuentaCoreApiResponse saldo = obtenerData(response);
            return new ConsultaSaldoCoreResponse(
                    Boolean.TRUE,
                    "SALDO_CONSULTADO",
                    "Saldo disponible consultado en Core.",
                    saldo.saldoDisponible()
            );
        } catch (RestClientResponseException exception) {
            ErrorCoreResponse error = leerErrorCore(exception);
            return new ConsultaSaldoCoreResponse(
                    Boolean.FALSE,
                    mapearCodigoConsultaSaldo(error.code(), exception.getStatusCode()),
                    mensajeCore(error.message(), "No fue posible consultar el saldo en Core."),
                    null
            );
        } catch (ResourceAccessException exception) {
            throw new IntegracionCoreException("CORE_NO_DISPONIBLE", MENSAJE_CORE_NO_DISPONIBLE, exception);
        }
    }

    @Override
    public ValidacionCuentaCoreResponse validarCuentaDestino(String numeroCuenta, String identificacionBeneficiario) {
        try {
            ApiResponseCore<ValidacionCuentaDestinoCoreApiResponse> response = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/api/v1/core/integracion-switch/cuentas/{numeroCuenta}/validacion-destino")
                            .queryParam("identificacionBeneficiario", identificacionBeneficiario)
                            .build(numeroCuenta))
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {
                    });

            ValidacionCuentaDestinoCoreApiResponse validacion = obtenerData(response);
            return new ValidacionCuentaCoreResponse(
                    Boolean.TRUE.equals(validacion.valida()),
                    mapearCodigoValidacionDestino(validacion.codigo()),
                    validacion.mensaje()
            );
        } catch (RestClientResponseException exception) {
            ErrorCoreResponse error = leerErrorCore(exception);
            return new ValidacionCuentaCoreResponse(
                    Boolean.FALSE,
                    mapearCodigoCore(error.code(), exception.getStatusCode()),
                    mensajeCore(error.message(), "No fue posible validar la cuenta destino en Core.")
            );
        } catch (ResourceAccessException exception) {
            throw new IntegracionCoreException("CORE_NO_DISPONIBLE", MENSAJE_CORE_NO_DISPONIBLE, exception);
        }
    }

    @Override
    public MovimientoCoreResponse ejecutarDebito(MovimientoCoreRequest movimientoCoreRequest) {
        try {
            TransferenciaCoreRequest request = construirTransferenciaRequest(movimientoCoreRequest);
            ApiResponseCore<TransferenciaCoreResponse> response = restClient.post()
                    .uri("/api/v1/core/integracion-switch/transacciones/transferencia")
                    .body(request)
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {
                    });

            TransferenciaCoreResponse transferencia = obtenerData(response);
            transferenciasPorOperacion.put(movimientoCoreRequest.uuidOperacionSwitch(), transferencia);
            registrarAuditoria("CORE_TRANSFERENCIA_EXITOSA", movimientoCoreRequest.uuidOperacionSwitch(), "COMPLETADA");
            return new MovimientoCoreResponse(
                    Boolean.TRUE,
                    "DEBITO_CORE_EXITOSO",
                    "Debito procesado por transferencia real en Core.",
                    transferencia.uuidDebitoCore(),
                    transferencia.uuidGrupoOperacion()
            );
        } catch (RestClientResponseException exception) {
            ErrorCoreResponse error = leerErrorCore(exception);
            registrarAuditoria("CORE_TRANSFERENCIA_RECHAZADA", movimientoCoreRequest.uuidOperacionSwitch(), mapearCodigoCore(error.code(), exception.getStatusCode()));
            return new MovimientoCoreResponse(
                    Boolean.FALSE,
                    mapearCodigoCore(error.code(), exception.getStatusCode()),
                    mensajeCore(error.message(), "El Core rechazo la transferencia."),
                    null,
                    movimientoCoreRequest.uuidGrupoCore()
            );
        } catch (ResourceAccessException exception) {
            registrarAuditoria("CORE_TRANSFERENCIA_ERROR_TECNICO", movimientoCoreRequest.uuidOperacionSwitch(), "CORE_NO_DISPONIBLE");
            throw new IntegracionCoreException("CORE_NO_DISPONIBLE", MENSAJE_CORE_NO_DISPONIBLE, exception);
        }
    }

    @Override
    public MovimientoCoreResponse ejecutarCredito(MovimientoCoreRequest movimientoCoreRequest) {
        TransferenciaCoreResponse transferencia = transferenciasPorOperacion.remove(
                movimientoCoreRequest.uuidOperacionSwitch()
        );
        if (transferencia == null) {
            return new MovimientoCoreResponse(
                    Boolean.FALSE,
                    "ERROR_CORE",
                    "No se encontro la transferencia previa para obtener el credito Core.",
                    null,
                    movimientoCoreRequest.uuidGrupoCore()
            );
        }

        return new MovimientoCoreResponse(
                Boolean.TRUE,
                "CREDITO_CORE_EXITOSO",
                "Credito procesado por transferencia real en Core.",
                transferencia.uuidCreditoCore(),
                transferencia.uuidGrupoOperacion()
        );
    }

    @Override
    public LiquidacionCoreResponse liquidarServicio(LiquidacionCoreRequest liquidacionCoreRequest) {
        try {
            ApiResponseCore<LiquidacionCoreApiResponse> response = restClient.post()
                    .uri("/api/v1/core/integracion-switch/transacciones/liquidacion-servicio")
                    .body(liquidacionCoreRequest)
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {
                    });

            LiquidacionCoreApiResponse liquidacion = obtenerData(response);
            registrarAuditoria("CORE_LIQUIDACION_EXITOSA", liquidacionCoreRequest.uuidGrupoOperacion(), "APLICADA");
            return new LiquidacionCoreResponse(
                    Boolean.TRUE,
                    "LIQUIDACION_CORE_EXITOSA",
                    "Liquidacion de servicio aplicada en Core.",
                    liquidacion.uuidDebitoMatriz(),
                    liquidacion.uuidCreditoIngresos(),
                    liquidacion.uuidCreditoIva(),
                    liquidacion.uuidGrupoOperacion()
            );
        } catch (RestClientResponseException exception) {
            ErrorCoreResponse error = leerErrorCore(exception);
            registrarAuditoria("CORE_LIQUIDACION_RECHAZADA", liquidacionCoreRequest.uuidGrupoOperacion(), mapearCodigoCore(error.code(), exception.getStatusCode()));
            return new LiquidacionCoreResponse(
                    Boolean.FALSE,
                    mapearCodigoCore(error.code(), exception.getStatusCode()),
                    mensajeCore(error.message(), "El Core rechazo la liquidacion del servicio."),
                    null,
                    null,
                    null,
                    liquidacionCoreRequest.uuidGrupoOperacion()
            );
        } catch (ResourceAccessException exception) {
            registrarAuditoria("CORE_LIQUIDACION_ERROR_TECNICO", liquidacionCoreRequest.uuidGrupoOperacion(), "CORE_NO_DISPONIBLE");
            throw new IntegracionCoreException("CORE_NO_DISPONIBLE", MENSAJE_CORE_NO_DISPONIBLE, exception);
        }
    }

    private TransferenciaCoreRequest construirTransferenciaRequest(MovimientoCoreRequest movimientoCoreRequest) {
        return new TransferenciaCoreRequest(
                movimientoCoreRequest.cuentaOrigen(),
                movimientoCoreRequest.cuentaDestino(),
                properties.getIntegration().getCodigoSubtipoPagoMasivo(),
                movimientoCoreRequest.monto(),
                movimientoCoreRequest.uuidOperacionSwitch(),
                movimientoCoreRequest.uuidGrupoCore(),
                movimientoCoreRequest.uuidOperacionSwitch().toString(),
                movimientoCoreRequest.concepto(),
                null,
                LocalDate.now(),
                null,
                null
        );
    }

    private AutenticacionCoreResponse construirAutenticacionMock(String usuario, String contrasena) {
        Boolean credencialesValidas = usuario != null
                && !usuario.isBlank()
                && contrasena != null
                && !contrasena.isBlank()
                && usuario.equals(properties.getIntegration().getMockUsuarioEmpresa());
        return new AutenticacionCoreResponse(
                credencialesValidas,
                credencialesValidas ? "EMPRESA" : null,
                credencialesValidas ? 1 : null,
                credencialesValidas ? 501 : null,
                credencialesValidas ? properties.getIntegration().getMockRucEmpresa() : null,
                usuario,
                credencialesValidas ? "Empresa mock pagos masivos" : null,
                credencialesValidas ? "EMPRESA_PAGOS_MASIVOS" : null,
                credencialesValidas ? "ACTIVO" : null,
                credencialesValidas
        );
    }

    private CuentaFavoritaPagosCoreResponse construirCuentaFavoritaMock(String ruc) {
        return new CuentaFavoritaPagosCoreResponse(
                ruc,
                Boolean.TRUE,
                properties.getIntegration().getMockCuentaFavoritaPagosNumero(),
                "ACTIVA",
                Boolean.TRUE,
                properties.getIntegration().getMockCuentaFavoritaPagosSaldoDisponible(),
                Boolean.TRUE,
                Boolean.TRUE,
                "CUENTA_FAVORITA_VALIDA",
                "Cuenta favorita valida para pagos masivos."
        );
    }

    private <T> T obtenerData(ApiResponseCore<T> response) {
        if (response == null || !Boolean.TRUE.equals(response.success()) || response.data() == null) {
            throw new IntegracionCoreException(
                    "ERROR_CORE",
                    "El Core respondio sin datos validos para la operacion solicitada."
            );
        }
        return response.data();
    }

    private ErrorCoreResponse leerErrorCore(RestClientResponseException exception) {
        try {
            ErrorCoreResponse error = objectMapper.readValue(exception.getResponseBodyAsString(), ErrorCoreResponse.class);
            if (error.code() != null || error.message() != null) {
                return error;
            }
        } catch (Exception ignored) {
            // La respuesta de error del Core puede no tener el formato estandar.
        }
        return new ErrorCoreResponse(
                Boolean.FALSE,
                exception.getStatusText(),
                exception.getResponseBodyAsString(),
                null
        );
    }

    private String mensajeCore(String mensaje, String mensajeDefault) {
        if (mensaje == null || mensaje.isBlank()) {
            return mensajeDefault;
        }
        return mensaje;
    }

    private String mapearCodigoValidacionDestino(String codigoCore) {
        if ("CUENTA_DESTINO_NO_PERTENECE_BENEFICIARIO".equals(codigoCore)) {
            return "BENEFICIARIO_NO_COINCIDE";
        }
        if ("CUENTA_DESTINO_NO_ACTIVA".equals(codigoCore)) {
            return "CUENTA_DESTINO_INACTIVA";
        }
        return codigoCore != null ? codigoCore : "ERROR_CORE";
    }

    private String mapearCodigoValidacionEmpresa(String codigoCore) {
        if ("EMPRESA_NO_EXISTE".equals(codigoCore)) {
            return "EMPRESA_NO_EXISTE";
        }
        if ("EMPRESA_NO_JURIDICA".equals(codigoCore)) {
            return "EMPRESA_NO_ES_JURIDICA";
        }
        if ("EMPRESA_INACTIVA".equals(codigoCore)) {
            return "EMPRESA_INACTIVA";
        }
        if ("PAGOS_MASIVOS_NO_ACTIVO".equals(codigoCore)) {
            return "EMPRESA_SIN_PAGOS_MASIVOS";
        }
        return codigoCore != null ? codigoCore : "ERROR_CORE";
    }

    private String mapearCodigoValidacionMatriz(String codigoCore) {
        if ("CUENTA_MATRIZ_NO_PERTENECE_EMPRESA".equals(codigoCore)) {
            return "CUENTA_MATRIZ_NO_PERTENECE_EMPRESA";
        }
        if ("CUENTA_MATRIZ_NO_ACTIVA".equals(codigoCore)) {
            return "CUENTA_ORIGEN_NO_ACTIVA";
        }
        if ("CUENTA_MATRIZ_NO_PERMITE_DEBITO".equals(codigoCore)) {
            return "CUENTA_ORIGEN_NO_ACTIVA";
        }
        return codigoCore != null ? codigoCore : "ERROR_CORE";
    }

    private String mapearCodigoValidacionCredencial(String codigoCore) {
        if ("CREDENCIAL_EMPRESARIAL_VALIDA".equals(codigoCore)) {
            return "CREDENCIAL_EMPRESARIAL_VALIDA";
        }
        if ("CREDENCIAL_NO_EXISTE".equals(codigoCore)) {
            return "CREDENCIAL_EMPRESARIAL_NO_EXISTE";
        }
        if ("CREDENCIAL_NO_PERTENECE_EMPRESA".equals(codigoCore)) {
            return "CREDENCIAL_EMPRESARIAL_NO_PERTENECE_EMPRESA";
        }
        if ("CREDENCIAL_INACTIVA".equals(codigoCore)) {
            return "CREDENCIAL_EMPRESARIAL_INACTIVA";
        }
        return codigoCore != null ? codigoCore : "ERROR_CORE";
    }

    private String mapearCodigoCuentaFavorita(String codigoCore) {
        if ("CUENTA_FAVORITA_NO_EXISTE".equals(codigoCore)) {
            return "CUENTA_FAVORITA_NO_EXISTE";
        }
        if ("CUENTA_FAVORITA_NO_ACTIVA".equals(codigoCore)) {
            return "CUENTA_FAVORITA_NO_ACTIVA";
        }
        if ("CUENTA_FAVORITA_NO_PERMITE_DEBITO".equals(codigoCore)) {
            return "CUENTA_FAVORITA_NO_PERMITE_DEBITO";
        }
        if ("EMPRESA_NO_EXISTE".equals(codigoCore)) {
            return "EMPRESA_NO_EXISTE";
        }
        if ("EMPRESA_NO_HABILITADA".equals(codigoCore)) {
            return "EMPRESA_NO_HABILITADA";
        }
        return codigoCore != null && !codigoCore.isBlank() ? codigoCore : "ERROR_CORE";
    }

    private Boolean esNoEncontrado(String codigoCore, HttpStatusCode statusCode) {
        return "RESOURCE_NOT_FOUND".equals(codigoCore) || (statusCode != null && statusCode.value() == 404);
    }

    private String mapearCodigoCore(String codigoCore, HttpStatusCode statusCode) {
        if ("RESOURCE_NOT_FOUND".equals(codigoCore)) {
            return "CUENTA_DESTINO_NO_EXISTE";
        }
        if ("INSUFFICIENT_FUNDS".equals(codigoCore)) {
            return "SALDO_INSUFICIENTE";
        }
        if ("ACCOUNT_NOT_ACTIVE".equals(codigoCore)) {
            return "CUENTA_ORIGEN_NO_ACTIVA";
        }
        if ("IDEMPOTENCY_ERROR".equals(codigoCore)) {
            return "TRANSACCION_DUPLICADA";
        }
        if (statusCode != null && statusCode.value() == 409) {
            return "TRANSACCION_DUPLICADA";
        }
        if (statusCode != null && statusCode.is5xxServerError()) {
            return "ERROR_CORE";
        }
        return codigoCore != null && !codigoCore.isBlank() ? codigoCore : "ERROR_CORE";
    }

    private String mapearCodigoConsultaSaldo(String codigoCore, HttpStatusCode statusCode) {
        if ("RESOURCE_NOT_FOUND".equals(codigoCore) || (statusCode != null && statusCode.value() == 404)) {
            return "CUENTA_ORIGEN_NO_EXISTE";
        }
        return mapearCodigoCore(codigoCore, statusCode);
    }

    private void registrarAuditoria(String accion, UUID uuidOperacion, String resultado) {
        registrarAuditoria(accion, uuidOperacion != null ? uuidOperacion.toString() : null, resultado);
    }

    private void registrarAuditoria(String accion, String idOperacion, String resultado) {
        try {
            ObjectNode datos = objectMapper.createObjectNode();
            datos.put("idOperacion", idOperacion);
            datos.put("resultado", resultado);
            RegistroAuditoriaRequest request = new RegistroAuditoriaRequest();
            request.setTipoActor(TipoActorAuditoria.SISTEMA);
            request.setIdActor("SWITCH");
            request.setAccion(accion);
            request.setEntidad("INTEGRACION_CORE");
            request.setIdEntidad(idOperacion != null ? idOperacion : "SIN_ID");
            request.setDatosDespues(datos);
            auditoriaSwitchService.registrarAccion(request);
        } catch (RuntimeException ignored) {
            // La auditoria no debe impedir que el Switch procese o registre el resultado financiero.
        }
    }
}
