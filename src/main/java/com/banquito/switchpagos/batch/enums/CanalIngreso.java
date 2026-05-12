package com.banquito.switchpagos.batch.enums;

import lombok.Getter;

@Getter
public enum CanalIngreso {
    PORTAL_WEB("PORTAL_WEB"),
    SFTP("SFTP"),
    API("API");

    private final String value;

    CanalIngreso(String value) {
        this.value = value;
    }
}
