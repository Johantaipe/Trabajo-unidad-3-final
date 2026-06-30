package com.vetexpert.usuarios.service;

import com.vetexpert.usuarios.dto.*;

/**
 * Servicio de autenticación: login, registro, recuperación de contraseña.
 */
public interface AuthService {

    LoginResponseDTO login(LoginRequestDTO request);

    ApiResponseDTO registrarCliente(RegistroClienteRequestDTO request);

    ApiResponseDTO solicitarRecuperacionPassword(OlvidePasswordRequestDTO request);

    ApiResponseDTO resetearPassword(ResetPasswordDTO request);

    ApiResponseDTO verificarOtp(VerificarOtpDTO request);
}
