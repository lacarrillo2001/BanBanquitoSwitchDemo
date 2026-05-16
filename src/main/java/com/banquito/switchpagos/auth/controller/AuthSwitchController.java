package com.banquito.switchpagos.auth.controller;

import com.banquito.switchpagos.auth.dto.api.LoginSwitchRequest;
import com.banquito.switchpagos.auth.dto.api.LoginSwitchResponse;
import com.banquito.switchpagos.auth.service.AuthSwitchService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/pagos-masivos/auth")
public class AuthSwitchController {

    private final AuthSwitchService authSwitchService;

    public AuthSwitchController(AuthSwitchService authSwitchService) {
        this.authSwitchService = authSwitchService;
    }

    @PostMapping("/login")
    public LoginSwitchResponse login(@RequestBody LoginSwitchRequest request) {
        return authSwitchService.login(request);
    }
}
