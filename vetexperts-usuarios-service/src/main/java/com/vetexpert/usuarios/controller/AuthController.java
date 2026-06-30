package com.vetexpert.usuarios.controller;

import com.vetexpert.usuarios.dto.*;
import com.vetexpert.usuarios.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador REST para autenticación y registro.
 * Endpoints públicos (no requieren JWT).
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * POST /api/auth/login — Autenticación con JWT.
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@Valid @RequestBody LoginRequestDTO request) {
        LoginResponseDTO response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    /**
     * POST /api/auth/registro-cliente — Registro público de clientes.
     */
    @PostMapping("/registro-cliente")
    public ResponseEntity<ApiResponseDTO> registrarCliente(
            @Valid @RequestBody RegistroClienteRequestDTO request) {
        ApiResponseDTO response = authService.registrarCliente(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * POST /api/auth/olvide-password — Solicitar recuperación de contraseña.
     */
    @PostMapping("/olvide-password")
    public ResponseEntity<ApiResponseDTO> olvidePassword(
            @Valid @RequestBody OlvidePasswordRequestDTO request) {
        ApiResponseDTO response = authService.solicitarRecuperacionPassword(request);
        return ResponseEntity.ok(response);
    }

    /**
     * POST /api/auth/verificar-otp — Verificar código OTP.
     */
    @PostMapping("/verificar-otp")
    public ResponseEntity<ApiResponseDTO> verificarOtp(
            @Valid @RequestBody VerificarOtpDTO request) {
        ApiResponseDTO response = authService.verificarOtp(request);
        return ResponseEntity.ok(response);
    }

    /**
     * POST /api/auth/reset-password — Restablecer contraseña con token.
     */
    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponseDTO> resetPassword(
            @Valid @RequestBody ResetPasswordDTO request) {
        ApiResponseDTO response = authService.resetearPassword(request);
        return ResponseEntity.ok(response);
    }
}
