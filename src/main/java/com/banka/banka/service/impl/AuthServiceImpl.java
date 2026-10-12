package com.banka.banka.service.impl;

import com.banka.banka.dto.request.LoginRequest;
import com.banka.banka.dto.response.AuthResponse;
import com.banka.banka.entity.User;
import com.banka.banka.repository.UserRepository;
import com.banka.banka.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı: " + request.getEmail()));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Geçersiz şifre");
        }

        // JWT token üretimi bu noktada yapılacak (security katmanı entegre edildiğinde)
        String token = "token-placeholder-for-" + user.getEmail();

        return AuthResponse.builder()
                .token(token)
                .build();
    }
}
