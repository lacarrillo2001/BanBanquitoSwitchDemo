package com.banquito.switchpagos.sftp.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "switch.sftp")
public class SftpProperties {

    private Boolean enabled;
    private String host;
    private Integer port;
    private String rootDirectory;
    private Long scanFixedDelayMs;
    private Long fileSettleMs;
    private Boolean failOnStartError;

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public String getRootDirectory() {
        return rootDirectory;
    }

    public void setRootDirectory(String rootDirectory) {
        this.rootDirectory = rootDirectory;
    }

    public Long getScanFixedDelayMs() {
        return scanFixedDelayMs;
    }

    public void setScanFixedDelayMs(Long scanFixedDelayMs) {
        this.scanFixedDelayMs = scanFixedDelayMs;
    }

    public Long getFileSettleMs() {
        return fileSettleMs;
    }

    public void setFileSettleMs(Long fileSettleMs) {
        this.fileSettleMs = fileSettleMs;
    }

    public Boolean getFailOnStartError() {
        return failOnStartError;
    }

    public void setFailOnStartError(Boolean failOnStartError) {
        this.failOnStartError = failOnStartError;
    }
}
