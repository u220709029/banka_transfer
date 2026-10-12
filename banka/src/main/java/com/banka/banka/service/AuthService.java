package com.banka.banka.service;

import com.banka.banka.dto.request.LoginRequest;
import com.banka.banka.dto.response.AuthResponse;

public interface AuthService {

    AuthResponse login(LoginRequest request);
}
