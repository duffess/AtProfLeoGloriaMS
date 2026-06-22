package com.example.auth_service.controller;

import com.example.auth_service.dto.AuthRequest;
import com.example.auth_service.dto.AuthResponse;
import com.example.auth_service.dto.RefreshRequest;
import com.example.auth_service.security.JwtTokenProvider;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final JwtTokenProvider tokenProvider;

    public AuthController(JwtTokenProvider tokenProvider) {
        this.tokenProvider = tokenProvider;
    }

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@RequestBody AuthRequest loginRequest) {
        // Usuário fixo para demonstração acadêmica
        if ("admin".equals(loginRequest.getUsername()) && "123456".equals(loginRequest.getPassword())) {
            String accessToken = tokenProvider.generateAccessToken(loginRequest.getUsername());
            String refreshToken = tokenProvider.generateRefreshToken(loginRequest.getUsername());

            return ResponseEntity.ok(new AuthResponse(accessToken, refreshToken));
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Credenciais inválidas");
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@RequestBody RefreshRequest refreshRequest) {
        String requestRefreshToken = refreshRequest.getRefreshToken();

        if (tokenProvider.validateToken(requestRefreshToken)) {
            String username = tokenProvider.getUsernameFromJWT(requestRefreshToken);
            
            String newAccessToken = tokenProvider.generateAccessToken(username);
            String newRefreshToken = tokenProvider.generateRefreshToken(username);
            
            return ResponseEntity.ok(new AuthResponse(newAccessToken, newRefreshToken));
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Refresh Token inválido ou expirado");
    }
}
