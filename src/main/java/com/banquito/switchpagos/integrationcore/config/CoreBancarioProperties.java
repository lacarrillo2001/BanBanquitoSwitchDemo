package com.banquito.switchpagos.integrationcore.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@ConfigurationProperties(prefix = "core")
public class CoreBancarioProperties {

    private String baseUrl;
    private Integration integration = new Integration();

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public Integration getIntegration() {
        return integration;
    }

    public void setIntegration(Integration integration) {
        this.integration = integration;
    }

    public static class Integration {
        private String mode;
        private String codigoSubtipoPagoMasivo;
        private String codigoCuentaIngresos;
        private String codigoCuentaIva;
        private String numeroCuentaIngresos;
        private String numeroCuentaIva;
        private Boolean mockAutenticacion;
        private Boolean mockCuentaFavoritaPagos;
        private String mockUsuarioEmpresa;
        private String mockRucEmpresa;
        private String mockCuentaFavoritaPagosNumero;
        private BigDecimal mockCuentaFavoritaPagosSaldoDisponible;

        public String getMode() {
            return mode;
        }

        public void setMode(String mode) {
            this.mode = mode;
        }

        public String getCodigoSubtipoPagoMasivo() {
            return codigoSubtipoPagoMasivo;
        }

        public void setCodigoSubtipoPagoMasivo(String codigoSubtipoPagoMasivo) {
            this.codigoSubtipoPagoMasivo = codigoSubtipoPagoMasivo;
        }

        public String getCodigoCuentaIngresos() {
            return codigoCuentaIngresos;
        }

        public void setCodigoCuentaIngresos(String codigoCuentaIngresos) {
            this.codigoCuentaIngresos = codigoCuentaIngresos;
        }

        public String getCodigoCuentaIva() {
            return codigoCuentaIva;
        }

        public void setCodigoCuentaIva(String codigoCuentaIva) {
            this.codigoCuentaIva = codigoCuentaIva;
        }

        public String getNumeroCuentaIngresos() {
            return numeroCuentaIngresos;
        }

        public void setNumeroCuentaIngresos(String numeroCuentaIngresos) {
            this.numeroCuentaIngresos = numeroCuentaIngresos;
        }

        public String getNumeroCuentaIva() {
            return numeroCuentaIva;
        }

        public void setNumeroCuentaIva(String numeroCuentaIva) {
            this.numeroCuentaIva = numeroCuentaIva;
        }

        public Boolean getMockAutenticacion() {
            return mockAutenticacion;
        }

        public void setMockAutenticacion(Boolean mockAutenticacion) {
            this.mockAutenticacion = mockAutenticacion;
        }

        public Boolean getMockCuentaFavoritaPagos() {
            return mockCuentaFavoritaPagos;
        }

        public void setMockCuentaFavoritaPagos(Boolean mockCuentaFavoritaPagos) {
            this.mockCuentaFavoritaPagos = mockCuentaFavoritaPagos;
        }

        public String getMockUsuarioEmpresa() {
            return mockUsuarioEmpresa;
        }

        public void setMockUsuarioEmpresa(String mockUsuarioEmpresa) {
            this.mockUsuarioEmpresa = mockUsuarioEmpresa;
        }

        public String getMockRucEmpresa() {
            return mockRucEmpresa;
        }

        public void setMockRucEmpresa(String mockRucEmpresa) {
            this.mockRucEmpresa = mockRucEmpresa;
        }

        public String getMockCuentaFavoritaPagosNumero() {
            return mockCuentaFavoritaPagosNumero;
        }

        public void setMockCuentaFavoritaPagosNumero(String mockCuentaFavoritaPagosNumero) {
            this.mockCuentaFavoritaPagosNumero = mockCuentaFavoritaPagosNumero;
        }

        public BigDecimal getMockCuentaFavoritaPagosSaldoDisponible() {
            return mockCuentaFavoritaPagosSaldoDisponible;
        }

        public void setMockCuentaFavoritaPagosSaldoDisponible(BigDecimal mockCuentaFavoritaPagosSaldoDisponible) {
            this.mockCuentaFavoritaPagosSaldoDisponible = mockCuentaFavoritaPagosSaldoDisponible;
        }
    }
}
