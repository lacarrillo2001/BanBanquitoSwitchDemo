package com.banquito.switchpagos.integrationcore.service.impl;

import com.banquito.switchpagos.integrationcore.dto.internal.ConsultaSaldoCoreResponse;
import com.banquito.switchpagos.integrationcore.dto.internal.CuentaFavoritaPagosCoreResponse;
import com.banquito.switchpagos.integrationcore.dto.internal.DiaHabilCoreResponse;
import com.banquito.switchpagos.integrationcore.dto.internal.AutenticacionCoreResponse;
import com.banquito.switchpagos.integrationcore.dto.internal.LiquidacionCoreRequest;
import com.banquito.switchpagos.integrationcore.dto.internal.LiquidacionCoreResponse;
import com.banquito.switchpagos.integrationcore.dto.internal.MovimientoCoreRequest;
import com.banquito.switchpagos.integrationcore.dto.internal.MovimientoCoreResponse;
import com.banquito.switchpagos.integrationcore.dto.internal.ValidacionCoreResponse;
import com.banquito.switchpagos.integrationcore.dto.internal.ValidacionCuentaCoreResponse;
import com.banquito.switchpagos.integrationcore.mapper.CoreBancarioMapper;
import com.banquito.switchpagos.integrationcore.service.CoreBancarioService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.UUID;

@Service
@ConditionalOnProperty(name = "core.integration.mode", havingValue = "stub")
public class CoreBancarioServiceStub implements CoreBancarioService {

    private static final BigDecimal SALDO_DISPONIBLE_SIMULADO = new BigDecimal("100000.00");
    private final CoreBancarioMapper coreBancarioMapper;
    private final com.banquito.switchpagos.integrationcore.config.CoreBancarioProperties coreBancarioProperties;

    public CoreBancarioServiceStub(CoreBancarioMapper coreBancarioMapper,
                                   com.banquito.switchpagos.integrationcore.config.CoreBancarioProperties coreBancarioProperties) {
        this.coreBancarioMapper = coreBancarioMapper;
        this.coreBancarioProperties = coreBancarioProperties;
    }

    @Override
    public AutenticacionCoreResponse autenticar(String usuario, String contrasena) {
        Boolean credencialesValidas = usuario != null && !usuario.isBlank()
                && contrasena != null && !contrasena.isBlank();
        return new AutenticacionCoreResponse(
                credencialesValidas,
                credencialesValidas ? "EMPRESA" : null,
                credencialesValidas ? 1 : null,
                credencialesValidas ? 501 : null,
                credencialesValidas ? coreBancarioProperties.getIntegration().getMockRucEmpresa() : null,
                usuario,
                credencialesValidas ? "Empresa simulada" : null,
                credencialesValidas ? "EMPRESA_PAGOS_MASIVOS" : null,
                credencialesValidas ? "ACTIVO" : null,
                credencialesValidas
        );
    }

    @Override
    public ValidacionCoreResponse validarEmpresa(String ruc) {
        return new ValidacionCoreResponse(
                Boolean.TRUE,
                "EMPRESA_HABILITADA",
                "Empresa habilitada para pagos masivos en el Core Bancario simulado."
        );
    }

    @Override
    public ValidacionCoreResponse validarCuentaMatriz(String ruc, String numeroCuenta) {
        return new ValidacionCoreResponse(
                Boolean.TRUE,
                "CUENTA_MATRIZ_VALIDA",
                "Cuenta matriz valida para pagos masivos en el Core Bancario simulado."
        );
    }

    @Override
    public CuentaFavoritaPagosCoreResponse obtenerCuentaFavoritaPagos(String ruc) {
        return new CuentaFavoritaPagosCoreResponse(
                ruc,
                Boolean.TRUE,
                coreBancarioProperties.getIntegration().getMockCuentaFavoritaPagosNumero(),
                "ACTIVA",
                Boolean.TRUE,
                SALDO_DISPONIBLE_SIMULADO,
                Boolean.TRUE,
                Boolean.TRUE,
                "CUENTA_FAVORITA_VALIDA",
                "Cuenta favorita valida para pagos masivos en el Core Bancario simulado."
        );
    }

    @Override
    public ValidacionCoreResponse validarCredencialEmpresa(String ruc, String username) {
        return new ValidacionCoreResponse(
                Boolean.TRUE,
                "CREDENCIAL_EMPRESA_VALIDA",
                "Credencial empresarial valida para pagos masivos en el Core Bancario simulado."
        );
    }

    @Override
    public DiaHabilCoreResponse consultarDiaHabil(LocalDate fecha) {
        Boolean esFinSemana = DayOfWeek.SATURDAY.equals(fecha.getDayOfWeek())
                || DayOfWeek.SUNDAY.equals(fecha.getDayOfWeek());
        LocalDate siguienteDiaHabil = fecha.plusDays(1);
        while (DayOfWeek.SATURDAY.equals(siguienteDiaHabil.getDayOfWeek())
                || DayOfWeek.SUNDAY.equals(siguienteDiaHabil.getDayOfWeek())) {
            siguienteDiaHabil = siguienteDiaHabil.plusDays(1);
        }
        return new DiaHabilCoreResponse(
                fecha,
                !esFinSemana,
                esFinSemana,
                Boolean.FALSE,
                siguienteDiaHabil,
                !esFinSemana ? "DIA_HABIL" : "DIA_NO_HABIL",
                !esFinSemana ? "La fecha indicada es dia habil." : "La fecha indicada no es dia habil."
        );
    }

    @Override
    public ConsultaSaldoCoreResponse consultarSaldoDisponible(String numeroCuenta) {
        return coreBancarioMapper.toConsultaSaldoResponse(SALDO_DISPONIBLE_SIMULADO);
    }

    @Override
    public ValidacionCuentaCoreResponse validarCuentaDestino(String numeroCuenta, String identificacionBeneficiario) {
        if (numeroCuenta != null && numeroCuenta.endsWith("0000")) {
            return coreBancarioMapper.toValidacionCuentaResponse(
                    Boolean.FALSE,
                    "CUENTA_DESTINO_NO_EXISTE",
                    "La cuenta destino no existe en el Core Bancario simulado."
            );
        }
        if (numeroCuenta != null && numeroCuenta.endsWith("9999")) {
            return coreBancarioMapper.toValidacionCuentaResponse(
                    Boolean.FALSE,
                    "CUENTA_DESTINO_BLOQUEADA",
                    "La cuenta destino se encuentra bloqueada en el Core Bancario simulado."
            );
        }
        return coreBancarioMapper.toValidacionCuentaResponse(
                Boolean.TRUE,
                "CUENTA_DESTINO_VALIDA",
                "Cuenta destino valida en el Core Bancario simulado."
        );
    }

    @Override
    public MovimientoCoreResponse ejecutarDebito(MovimientoCoreRequest movimientoCoreRequest) {
        if (!Boolean.TRUE.equals(movimientoCoreRequest.permiteSobregiro())
                && movimientoCoreRequest.monto().compareTo(SALDO_DISPONIBLE_SIMULADO) > 0) {
            return coreBancarioMapper.toMovimientoResponse(
                    Boolean.FALSE,
                    "SALDO_INSUFICIENTE",
                    "La cuenta matriz no tiene saldo suficiente en el Core Bancario simulado.",
                    null,
                    movimientoCoreRequest.uuidGrupoCore()
            );
        }
        UUID uuidGrupoCore = movimientoCoreRequest.uuidGrupoCore() != null
                ? movimientoCoreRequest.uuidGrupoCore()
                : UUID.randomUUID();
        return coreBancarioMapper.toMovimientoResponse(
                Boolean.TRUE,
                "DEBITO_CORE_EXITOSO",
                "Debito simulado ejecutado correctamente.",
                UUID.randomUUID(),
                uuidGrupoCore
        );
    }

    @Override
    public MovimientoCoreResponse ejecutarCredito(MovimientoCoreRequest movimientoCoreRequest) {
        return coreBancarioMapper.toMovimientoResponse(
                Boolean.TRUE,
                "CREDITO_CORE_EXITOSO",
                "Credito simulado ejecutado correctamente.",
                UUID.randomUUID(),
                movimientoCoreRequest.uuidGrupoCore() != null ? movimientoCoreRequest.uuidGrupoCore() : UUID.randomUUID()
        );
    }

    @Override
    public LiquidacionCoreResponse liquidarServicio(LiquidacionCoreRequest liquidacionCoreRequest) {
        UUID uuidGrupoCore = liquidacionCoreRequest.uuidGrupoOperacion() != null
                ? liquidacionCoreRequest.uuidGrupoOperacion()
                : UUID.randomUUID();
        return new LiquidacionCoreResponse(
                Boolean.TRUE,
                "LIQUIDACION_CORE_EXITOSA",
                "Liquidacion simulada ejecutada correctamente.",
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                uuidGrupoCore
        );
    }
}
