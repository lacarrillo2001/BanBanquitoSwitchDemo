package com.banquito.switchpagos.auth.service;

import com.banquito.switchpagos.auth.dto.api.LoginSwitchRequest;
import com.banquito.switchpagos.auth.dto.api.LoginSwitchResponse;

public interface AuthSwitchService {

    LoginSwitchResponse login(LoginSwitchRequest request);
}
