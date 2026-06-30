package com.vetexpert.usuarios.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO para restablecer contraseña con token.
 */
public class ResetPasswordDTO {

    @NotBlank(message = "El token es obligatorio")
    private String token;

    @NotBlank(message = "La nueva contraseña es obligatoria")
    @Size(min = 8, message = "Debe tener al menos 8 caracteres")
    private String passwordNueva;

    @NotBlank(message = "Debe confirmar la nueva contraseña")
    private String confirmPasswordNueva;

    public ResetPasswordDTO() {}

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public String getPasswordNueva() { return passwordNueva; }
    public void setPasswordNueva(String passwordNueva) { this.passwordNueva = passwordNueva; }

    public String getConfirmPasswordNueva() { return confirmPasswordNueva; }
    public void setConfirmPasswordNueva(String confirmPasswordNueva) { this.confirmPasswordNueva = confirmPasswordNueva; }
}
